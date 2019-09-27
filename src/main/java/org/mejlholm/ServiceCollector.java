package org.mejlholm;

import io.fabric8.kubernetes.api.model.Service;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
@Traced
public class ServiceCollector {

    private final KubernetesClient kubernetesClient;

    public ServiceCollector(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    private Map<String, String> knownServices = new HashMap<>();
    private List<ServiceResult> ingressedServices = new ArrayList<>();
    private List<ServiceResult> nonIngressedServices = new ArrayList<>();


    List<ServiceResult> getIngressedServices() {
        return ingressedServices;
    }

    List<ServiceResult> getNonIngressedServices() {
        return nonIngressedServices;
    }

    @ConfigProperty(name = "NAMESPACE")
    private String namespace;


    @Scheduled(every = "10m")
    void collectServices() {

        Client client = ClientBuilder.newClient();

        List<Ingress> ingresses = kubernetesClient.extensions().ingresses().inNamespace(namespace).list().getItems();

        List<ServiceResult> results = new ArrayList<>();
        for (Ingress i: ingresses) {

            String serviceName = i.getMetadata().getName();

            //todo, the edge case needs around rules need some more thought, can we only run into http?
            IngressRule rule = i.getSpec().getRules().get(0);
            final String openapiUrl = "http://" + rule.getHost() + "/openapi";
            knownServices.put(serviceName, serviceName);
            results.add(new ServiceResult(serviceName, openapiUrl, getOpenapiUiUrl(client, rule), parseOpenapi(openapiUrl)));
        }

        ingressedServices = results;

        List<Service> services = kubernetesClient.services().inNamespace(namespace).list().getItems();
        nonIngressedServices = services.stream()
                .filter(s -> !knownServices.containsKey(s.getMetadata().getName()))
                .map(s -> new ServiceResult(s.getMetadata().getName(), null, null, parseOpenapi("http://" + s.getSpec().getClusterIP() + ":" + s.getSpec().getPorts().get(0).getPort() + "/openapi")))
                .collect(Collectors.toList());

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

    private List<PathResult> parseOpenapi(String baseUrl) {

        System.out.println(baseUrl);
        List<PathResult> results = new ArrayList<>();

        OpenAPI openAPI;
        try {
            InputStream input = new URL(baseUrl).openStream();
            openAPI = OpenApiParser.parse(input, OpenApiSerializer.Format.YAML);
        } catch (IOException e) {
            log.info("Unable to open url: " + baseUrl);
            return results;
        }

        Paths paths = openAPI.getPaths();

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
