package com.example.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "JMS Consumer API");
        
        return Response.ok(health).build();
    }
}
