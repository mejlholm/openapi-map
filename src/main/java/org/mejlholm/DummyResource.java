package org.mejlholm;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Metered;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/dummy")
@ApplicationScoped
@Metered
@Slf4j
public class DummyResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)

    public void getServices() {
        System.out.println("Nothing");
    }

    @POST
    public void createServices() {
        System.out.println("Nothing");
    }

    @DELETE
    public void deleteServices() {
        System.out.println("Nothing");
    }

}