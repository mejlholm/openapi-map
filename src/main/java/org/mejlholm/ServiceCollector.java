package org.mejlholm;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.opentracing.Traced;
import org.mejlholm.model.PathResult;
import org.mejlholm.model.ServiceResult;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
@Traced
public class ServiceCollector {

    private final KubernetesClient kubernetesClient;

    public ServiceCollector(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    private List<ServiceResult> services = new ArrayList<>();

    List<ServiceResult> getServices() {
        return services;
    }

    @Scheduled(every = "10m")
    void collectServices() {

        final String namespace = "apps";
        List<Ingress> ingresses = kubernetesClient.extensions().ingresses().inNamespace(namespace).list().getItems();

        List<ServiceResult> results = new ArrayList<>();
        for (Ingress i: ingresses) {

            //fixme, the edge case needs some more thought
            List<PathResult> pathResults = null;
            IngressRule rule = i.getSpec().getRules().get(0);
            final String url = "http://" + rule.getHost() + "/openapi";
            try {
                pathResults = parseOpenapi(url);
            } catch (IOException e) {
                log.info("Unable to open url: " + url);
            }

            results.add(new ServiceResult(i.getMetadata().getName(), url, pathResults));
        }
        services = results;
    }

    private List<PathResult> parseOpenapi(String baseUrl) throws IOException {
        InputStream input = new URL(baseUrl).openStream();
        OpenAPI openAPI = OpenApiParser.parse(input, OpenApiSerializer.Format.YAML);
        Paths paths = openAPI.getPaths();

        List<PathResult> results = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths.getPathItems().entrySet()) {
            String pathName = entry.getKey();

            PathItem pathItem = entry.getValue();
            List<String> operations = new ArrayList<>();

            if (pathItem.getDELETE() != null) {
                operations.add("DELETE");
            }
            if (pathItem.getGET() != null) {
                operations.add("GET");
            }
            if (pathItem.getHEAD() != null) {
                operations.add("HEAD");
            }
            if (pathItem.getOPTIONS() != null) {
                operations.add("OPTIONS");
            }
            if (pathItem.getPATCH() != null) {
                operations.add("PATCH");
            }
            if (pathItem.getPOST() != null) {
                operations.add("POST");
            }
            if (pathItem.getPUT() != null) {
                operations.add("PUT");
            }
            if (pathItem.getTRACE() != null) {
                operations.add("TRACE");
            }

            results.add(new PathResult(pathName, operations));
        }
        return results;
    }

}