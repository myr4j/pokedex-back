package com.example.rest;

import com.example.dto.TrainerMessage;
import com.example.service.MessageLogService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// endpoint rest pour les messages de creation de trainer
@Path("/creations")
@Produces(MediaType.APPLICATION_JSON)
public class CreationsResource {
    
    private final MessageLogService messageLogService;
    
    public CreationsResource() {
        this.messageLogService = MessageLogService.getInstance();
    }

    // retourne toutes les creations de trainer
    @GET
    public Response getAllCreations() {
        List<TrainerMessage> messages = messageLogService.getAllTrainerMessages();
        return Response.ok(messages).build();
    }

    // retourne les n creations les plus recentes
    @GET
    @Path("/recent")
    public Response getRecentCreations(@QueryParam("limit") @DefaultValue("10") int limit) {
        if (limit < 1 || limit > 100) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Limit must be between 1 and 100").build();
        }
        
        List<TrainerMessage> messages = messageLogService.getTrainerMessages(limit);
        return Response.ok(messages).build();
    }

    // retourne les statistiques des creations
    @GET
    @Path("/stats")
    public Response getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", messageLogService.getTotalTrainerCount());
        stats.put("maxMessages", 100);
        
        return Response.ok(stats).build();
    }
    
}
