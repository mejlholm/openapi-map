package org.mejlholm;


import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.UUID;


@Provider
@Slf4j
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    UriInfo info;

    @Context
    HttpServletRequest request;

    @Inject
    Tracer tracer;

    @Override
    public void filter(ContainerRequestContext context) {

        final String method = context.getMethod();
        final String path = info.getPath();
        final String address = request.getRemoteAddr();

        String uuid = tracer.activeSpan().getBaggageItem("uuid");
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            tracer.activeSpan().setBaggageItem("uuid", uuid);
        }

        log.info("Request [UUID=" + uuid +"] " + method + " " + path + " from IP " + address);

        tracer.activeSpan().setTag("uuid", uuid).setTag("method", method).setTag("path", path).setTag("address", address);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (responseContext != null && tracer.activeSpan() != null && tracer.activeSpan().getBaggageItem("uuid") != null) {
            log.info("Response [UUID=" + tracer.activeSpan().getBaggageItem("uuid") + "] status code " + responseContext.getStatus());
        } else {
            log.info("No response context");
        }
    }
}