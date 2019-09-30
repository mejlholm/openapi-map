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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
@Traced
public class ServiceScraper {

    private final KubernetesClient kubernetesClient;

    public ServiceScraper(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    private Map<String, String> knownServices = new HashMap<>();
    private List<PathResult> ingressedServices = new ArrayList<>();
    private List<PathResult> nonIngressedServices = new ArrayList<>();


    List<PathResult> getIngressedServices() {
        return ingressedServices;
    }

    List<PathResult> getNonIngressedServices() {
        return nonIngressedServices;
    }

    @ConfigProperty(name = "NAMESPACE")
    private String namespace;


    @Scheduled(every = "10m")
    void collectServices() {

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(1, TimeUnit.SECONDS);
        clientBuilder.readTimeout(1, TimeUnit.SECONDS);
        Client client = clientBuilder.build();

        List<Ingress> ingresses = kubernetesClient.extensions().ingresses().inNamespace(namespace).list().getItems();

        List<PathResult> ingressResults = new ArrayList<>();
        for (Ingress i: ingresses) {
            String serviceName = i.getMetadata().getName();

            //todo, the edge case needs around rules need some more thought, can we only run into http?
            IngressRule rule = i.getSpec().getRules().get(0);
            final String openapiUrl = "http://" + rule.getHost() + "/openapi";
            knownServices.put(serviceName, serviceName);
            ingressResults.addAll(parseOpenapi(serviceName, openapiUrl, getOpenapiUiUrl(client, rule.getHost()), openapiUrl));
        }

        ingressedServices = ingressResults;



        List<Service> services = kubernetesClient.services().inNamespace(namespace).list().getItems().stream()
                .filter(s -> knownServices.containsKey(s.getMetadata().getName()))
                .collect(Collectors.toList());
        List<PathResult> serviceResults = new ArrayList<>();
        for (Service s: services) {
            String serviceName = s.getMetadata().getName();
            final String openapiUrl = "http://" + s.getSpec().getClusterIP() + ":" + s.getSpec().getPorts().get(0).getPort() + "/openapi";
            serviceResults.addAll(parseOpenapi(serviceName, null, null, openapiUrl)); //we won't provide links to url's that can't be reached.
        }

        nonIngressedServices = serviceResults;

        client.close();
    }

    private String getOpenapiUiUrl(Client client, String host) {
        String[] possibleUiPaths = {"/openapi/ui/", "/swagger-ui/"}; //other???
        for (String path: possibleUiPaths) {
            String fullPath = "http://" + host + path;
            Response response = client.target(fullPath).request().get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return fullPath;
            }
        }
        return null;
    }

    private List<PathResult> parseOpenapi(String name, String openapiUiUrl, String openapiUrl, String baseUrl) {

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
            results.add(new PathResult(name, openapiUiUrl, openapiUrl, entry.getKey(), getPathOperations(entry.getValue())));
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
