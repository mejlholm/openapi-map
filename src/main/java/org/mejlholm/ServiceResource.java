package org.mejlholm;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.mejlholm.model.ServiceResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/service")
@ApplicationScoped
@Metered
@Slf4j
public class ServiceResource {

    @Inject
    ServiceCollector serviceCollector;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceResult> getServices() {
        return serviceCollector.getServices();
    }
}