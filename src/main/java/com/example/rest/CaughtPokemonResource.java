package com.example.rest;

import com.example.domain.CaughtPokemon;
import com.example.security.Secured;
import com.example.service.CaughtPokemonService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/caught-pokemons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class CaughtPokemonResource {

    @Inject
    private CaughtPokemonService caughtPokemonService;

    @POST
    public Response createCaughtPokemon(Map<String, Long> request) {
        Long trainerId = request.get("trainerId");
        Long pokemonId = request.get("pokemonId");
        
        if (trainerId == null || pokemonId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("trainerId and pokemonId are required").build();
        }
        
        try {
            CaughtPokemon created = caughtPokemonService.createCaughtPokemon(trainerId, pokemonId);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    public Response getAllCaughtPokemons() {
        List<CaughtPokemon> caughtPokemons = caughtPokemonService.findAllCaughtPokemons();
        return Response.ok(caughtPokemons).build();
    }

    @GET
    @Path("/{id}")
    public Response getCaughtPokemonById(@PathParam("id") Long id) {
        CaughtPokemon caughtPokemon = caughtPokemonService.findCaughtPokemonById(id);
        if (caughtPokemon == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(caughtPokemon).build();
    }

    @GET
    @Path("/trainer/{trainerId}")
    public Response getCaughtPokemonsByTrainer(@PathParam("trainerId") Long trainerId) {
        List<CaughtPokemon> caughtPokemons = caughtPokemonService.findCaughtPokemonsByTrainer(trainerId);
        return Response.ok(caughtPokemons).build();
    }

    @GET
    @Path("/pokemon/{pokemonId}")
    public Response getCaughtPokemonsByPokemon(@PathParam("pokemonId") Long pokemonId) {
        List<CaughtPokemon> caughtPokemons = caughtPokemonService.findCaughtPokemonsByPokemon(pokemonId);
        return Response.ok(caughtPokemons).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCaughtPokemon(@PathParam("id") Long id) {
        caughtPokemonService.deleteCaughtPokemon(id);
        return Response.noContent().build();
    }
}

