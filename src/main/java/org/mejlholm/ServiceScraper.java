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
import org.mejlholm.model.Annotation;
import org.mejlholm.model.PathResult;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private List<PathResult> services = new ArrayList<>();

    @ConfigProperty(name = "NAMESPACE", defaultValue = "default")
    String namespace;

    private static final String SCRAPE_ANNOTATION = "openapi-map/scrape";

    @Scheduled(every = "10m")
    void scrape() {

        Set<String> knownServices = new HashSet<>();

        services = scrapeIngressedServices(knownServices);
        services.addAll(scrapeServices(knownServices));

        services.sort(Comparator.comparing(PathResult::getName));
    }

    private List<PathResult> scrapeIngressedServices(Set knownServices) {
        /* setup client */
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(1, TimeUnit.SECONDS);
        clientBuilder.readTimeout(1, TimeUnit.SECONDS);
        Client client = clientBuilder.build();

        List<Ingress> ingressItems = kubernetesClient.extensions().ingresses().inNamespace(namespace).list().getItems();

        /* use ingressed services before plain services, as they are accessible from the outside */
        List<PathResult> ingressResults = new ArrayList<>();
        for (Ingress i : ingressItems) {
            if (i.getMetadata() != null) {

                List<Annotation> scrapedAnnotations = new ArrayList<>();
                if (i.getMetadata().getAnnotations() != null) {
                    if (i.getMetadata().getAnnotations().containsKey(SCRAPE_ANNOTATION) && i.getMetadata().getAnnotations().get(SCRAPE_ANNOTATION).equalsIgnoreCase("false")) {
                        continue;
                    }

                    if (i.getMetadata().getAnnotations() != null) {
                        for (Map.Entry<String, String> entry : i.getMetadata().getAnnotations().entrySet()) {
                            if (entry.getKey().contains("openapi-map") && !entry.getKey().contains(SCRAPE_ANNOTATION)) {
                                scrapedAnnotations.add(new Annotation(entry.getKey(), entry.getValue()));
                            }
                        }
                    }
                }

                //todo, the edge case needs around rules need some more thought, can we only run into http?
                String serviceName = i.getMetadata().getName();
                IngressRule rule = i.getSpec().getRules().get(0);
                final String openapiUrl = "http://" + rule.getHost() + "/openapi";
                knownServices.add(serviceName);
                ingressResults.addAll(parseOpenapi(serviceName, openapiUrl, getOpenapiUiUrl(client, rule.getHost()), openapiUrl, scrapedAnnotations));
            }
        }

        client.close();

        return ingressResults;
    }

    private List<PathResult> scrapeServices(Set knownServices) {
        List<Service> serviceItems = kubernetesClient.services().inNamespace(namespace).list().getItems().stream()
                .filter(s -> !knownServices.contains(s.getMetadata().getName()))
                .collect(Collectors.toList());
        List<PathResult> serviceResults = new ArrayList<>();
        for (Service s : serviceItems) {
            if (s.getMetadata() != null) {

                List<Annotation> scrapedAnnotations = new ArrayList<>();
                if (s.getMetadata().getAnnotations() != null) {
                    if (s.getMetadata().getAnnotations().containsKey(SCRAPE_ANNOTATION) && s.getMetadata().getAnnotations().get(SCRAPE_ANNOTATION).equalsIgnoreCase("false")) {
                        continue;
                    }
                }

                if (s.getMetadata().getAnnotations() != null) {
                    for (Map.Entry<String, String> entry : s.getMetadata().getAnnotations().entrySet()) {
                        if (entry.getKey().contains("openapi-map") && !entry.getKey().contains(SCRAPE_ANNOTATION)) {
                            scrapedAnnotations.add(new Annotation(entry.getKey(), entry.getValue()));
                        }
                    }
                }

                String serviceName = s.getMetadata().getName();
                final String openapiUrl = "http://" + s.getSpec().getClusterIP() + ":" + s.getSpec().getPorts().get(0).getPort() + "/openapi";
                serviceResults.addAll(parseOpenapi(serviceName, null, null, openapiUrl, scrapedAnnotations)); //we won't provide links to url's that can't be reached.
            }
        }
        return serviceResults;
    }

    private String getOpenapiUiUrl(Client client, String host) {
        String[] possibleUiPaths = {"/swagger-ui/", "/openapi/ui/"}; //other???
        for (String path: possibleUiPaths) {
            String fullPath = "http://" + host + path;
            Response response = client.target(fullPath).request().get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return fullPath;
            }
        }
        return null;
    }

    private List<PathResult> parseOpenapi(String name, String openapiUiUrl, String openapiUrl, String baseUrl, List<Annotation> annotations) {

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
            results.add(new PathResult(name, openapiUiUrl, openapiUrl, entry.getKey(), getPathMethods(entry.getValue()), annotations));
        }
        return results;
    }


    private List<String> getPathMethods(PathItem pathItem) {
        List<String> methods = new ArrayList<>();

        if (pathItem.getDELETE() != null) {
            methods.add("DELETE");
        }
        if (pathItem.getGET() != null) {
            methods.add("GET");
        }
        if (pathItem.getHEAD() != null) {
            methods.add("HEAD");
        }
        if (pathItem.getOPTIONS() != null) {
            methods.add("OPTIONS");
        }
        if (pathItem.getPATCH() != null) {
            methods.add("PATCH");
        }
        if (pathItem.getPOST() != null) {
            methods.add("POST");
        }
        if (pathItem.getPUT() != null) {
            methods.add("PUT");
        }
        if (pathItem.getTRACE() != null) {
            methods.add("TRACE");
        }

        return methods;
    }

    List<PathResult> getServices() {
        return services;
    }

}
