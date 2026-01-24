package com.example.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Method;

// filtre tres simple qui verifie qu'un trainer est connecte pour les endpoints @secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Class<?> resourceClass = resourceInfo.getResourceClass();
        Method resourceMethod = resourceInfo.getResourceMethod();

        boolean classSecured = resourceClass != null && resourceClass.isAnnotationPresent(Secured.class);
        boolean methodSecured = resourceMethod != null && resourceMethod.isAnnotationPresent(Secured.class);

        if (!classSecured && !methodSecured) {
            // endpoint public, pas besoin de verifier
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("trainerId") == null) {
            // pas connecte donc on retourne 401
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("authentication required")
                            .build()
            );
        }
    }
}
