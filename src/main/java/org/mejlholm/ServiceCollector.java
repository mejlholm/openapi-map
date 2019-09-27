package org.mejlholm;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.opentracing.Traced;
import org.mejlholm.model.PathResult;
import org.mejlholm.model.ServiceResult;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
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

    @ConfigProperty(name = "NAMESPACE")
    private String namespace;


    @Scheduled(every = "10m")
    void collectServices() {

        Client client = ClientBuilder.newClient(); // TODO: 9/27/19 cache better

        List<Ingress> ingresses = kubernetesClient.extensions().ingresses().inNamespace(namespace).list().getItems();

        List<ServiceResult> results = new ArrayList<>();
        for (Ingress i: ingresses) {

            //todo, the edge case needs around rules need some more thought, can we only run into http?
            List<PathResult> pathResults = null;
            IngressRule rule = i.getSpec().getRules().get(0);
            final String openapiUrl = "http://" + rule.getHost() + "/openapi";
            try {
                pathResults = parseOpenapi(openapiUrl);
            } catch (IOException e) {
                log.info("Unable to open url: " + openapiUrl);
            }


            results.add(new ServiceResult(i.getMetadata().getName(), openapiUrl, getOpenapiUiUrl(client, rule), pathResults));
        }

        services = results;

        // TODO: 9/26/19 make overview of non-ingressed services 

        // TODO: 9/27/19 filter using labels from kubernetes

        client.close();
    }

    private String getOpenapiUiUrl(Client client, IngressRule rule) {


        String[] possibleUiPaths = {"/openapi/ui", "/swagger-ui"}; //other???
        for (String path: possibleUiPaths) {
            String fullPath = "http://" + rule.getHost() + "/" + path;
            Response response = client.target(fullPath).request().get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return fullPath;
            }
        }


        return null;
    }

    private List<PathResult> parseOpenapi(String baseUrl) throws IOException {
        InputStream input = new URL(baseUrl).openStream();
        OpenAPI openAPI = OpenApiParser.parse(input, OpenApiSerializer.Format.YAML);
        Paths paths = openAPI.getPaths();

        List<PathResult> results = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths.getPathItems().entrySet()) {
            results.add(new PathResult(entry.getKey(), getPathOperations(entry.getValue())));
        }
        return results;
    }


    private List<String> getPathOperations(PathItem pathItem) {
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

        return operations;
    }

}
