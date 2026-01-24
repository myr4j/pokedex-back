package com.example.rest;

import com.example.domain.Trainer;
import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.dto.TrainerMessage;
import com.example.messaging.TrainerMessageProducer;
import com.example.service.AuthService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    @Inject
    private TrainerMessageProducer trainerMessageProducer;

    @Context
    private HttpServletRequest httpRequest;

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("name, email and password are required").build();
        }

        try {
            Trainer trainer = authService.register(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword()
            );

            // envoyer un message jms pour la creation du trainer
            TrainerMessage trainerMessage = new TrainerMessage(
                    trainer.getId(),
                    trainer.getName(),
                    trainer.getEmail()
            );
            trainerMessageProducer.sendTrainerCreatedMessage(trainerMessage);

            AuthResponse response = new AuthResponse(
                    trainer.getId(),
                    trainer.getEmail(),
                    trainer.getName()
            );

            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("email and password are required").build();
        }

        try {
            Trainer trainer = authService.login(request.getEmail(), request.getPassword());

            // stocker l'id du trainer dans la session http pour les appels suivants
            jakarta.servlet.http.HttpSession session = httpRequest.getSession(true);
            session.setAttribute("trainerId", trainer.getId());

            AuthResponse response = new AuthResponse(
                    trainer.getId(),
                    trainer.getEmail(),
                    trainer.getName()
            );

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        // invalider la session http
        if (httpRequest.getSession(false) != null) {
            httpRequest.getSession().invalidate();
        }
        return Response.ok("logged out successfully").build();
    }
}
