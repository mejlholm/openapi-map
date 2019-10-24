package org.mejlholm;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Metered;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("services")
@ApplicationScoped
@Metered
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    @Inject
    ServiceScraper serviceScraper;

    @ConfigProperty(name = "NAMESPACE", defaultValue = "default")
    String namespace;

    @GET
    @Path("")
    public Response getServices() {
        return Response.ok().entity(serviceScraper.getServices()).build();
    }

    @GET
    @Path("scrape")
    public Response scrape() {
        serviceScraper.scrape();
        return Response.ok().build();
    }

    @GET
    @Path("namespace")
    public Response getNamespace() {
        JsonObject payload = Json.createObjectBuilder()
                .add("namespace", namespace).build();

        return Response.ok().entity(payload.toString()).build();
    }
}