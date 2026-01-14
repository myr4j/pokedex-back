package com.example.rest;

import com.example.domain.Trainer;
import com.example.dto.TrainerStats;
import com.example.security.Secured;
import com.example.service.TrainerService;
import com.example.service.TrainerStatsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/trainers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class TrainerResource {

    @Inject
    private TrainerService trainerService;

    @Inject
    private TrainerStatsService trainerStatsService;

    @POST
    public Response createTrainer(Trainer trainer) {
        Trainer created = trainerService.createTrainer(trainer.getName(), trainer.getEmail());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    public Response getAllTrainers() {
        List<Trainer> trainers = trainerService.findAllTrainers();
        return Response.ok(trainers).build();
    }

    @GET
    @Path("/{id}")
    public Response getTrainerById(@PathParam("id") Long id) {
        Trainer trainer = trainerService.findTrainerById(id);
        if (trainer == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(trainer).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTrainer(@PathParam("id") Long id, Trainer trainer) {
        Trainer updated = trainerService.updateTrainer(id, trainer.getName(), trainer.getEmail());
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTrainer(@PathParam("id") Long id) {
        trainerService.deleteTrainer(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/stats")
    public Response getTrainerStats(@PathParam("id") Long id) {
        try {
            TrainerStats stats = trainerStatsService.getTrainerStats(id);
            return Response.ok(stats).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage()).build();
        }
    }
}

