package com.example.rest;

import com.example.dto.CaptureMessage;
import com.example.service.MessageLogService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// endpoint rest pour les messages de capture de pokemon
@Path("/captures")
@Produces(MediaType.APPLICATION_JSON)
public class CapturesResource {
    
    private final MessageLogService messageLogService;
    
    public CapturesResource() {
        this.messageLogService = MessageLogService.getInstance();
    }

    // retourne toutes les captures
    @GET
    public Response getAllCaptures() {
        List<CaptureMessage> messages = messageLogService.getAllCaptureMessages();
        return Response.ok(messages).build();
    }

    // retourne les n captures les plus recentes
    @GET
    @Path("/recent")
    public Response getRecentCaptures(@QueryParam("limit") @DefaultValue("10") int limit) {
        if (limit < 1 || limit > 100) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Limit must be between 1 and 100").build();
        }
        
        List<CaptureMessage> messages = messageLogService.getCaptureMessages(limit);
        return Response.ok(messages).build();
    }

    // retourne les statistiques des captures
    @GET
    @Path("/stats")
    public Response getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", messageLogService.getTotalCaptureCount());
        stats.put("maxMessages", 100);
        
        return Response.ok(stats).build();
    }
    
}
