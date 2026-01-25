package com.example.rest;

import com.example.dto.AggregatedCaptureStats;
import com.example.service.CaptureAggregator;
import com.example.service.MessageLogService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;


@Path("/aggregated")
@Produces(MediaType.APPLICATION_JSON)
public class AggregatedStatsResource {
    
    private final MessageLogService messageLogService;
    private final CaptureAggregator captureAggregator;
    
    public AggregatedStatsResource() {
        this.messageLogService = MessageLogService.getInstance();
        this.captureAggregator = CaptureAggregator.getInstance();
    }
    
    // retourne les statistiques agregees pour tous les trainers
    @GET
    @Path("/stats")
    public Response getAllAggregatedStats() {
        List<AggregatedCaptureStats> stats = captureAggregator.aggregateAllTrainers(
                messageLogService.getAllCaptureMessages()
        );
        return Response.ok(stats).build();
    }
    
    // retourne les statistiques agregees pour un trainer specifique
    @GET
    @Path("/stats/trainer/{trainerId}")
    public Response getTrainerAggregatedStats(@PathParam("trainerId") Long trainerId) {
        if (trainerId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("trainerId is required").build();
        }
        
        AggregatedCaptureStats stats = captureAggregator.aggregateByTrainer(
                trainerId,
                messageLogService.getAllCaptureMessages()
        );
        
        if (stats == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("no captures found for trainer id: " + trainerId).build();
        }
        
        return Response.ok(stats).build();
    }
}
