package com.example.rest;

import com.example.domain.Pokemon;
import com.example.dto.PokemonComparison;
import com.example.security.Secured;
import com.example.service.PokemonComparisonService;
import com.example.service.PokemonService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/pokemons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class PokemonResource {

    @Inject
    private PokemonService pokemonService;

    @Inject
    private PokemonComparisonService pokemonComparisonService;

    @POST
    public Response createPokemon(Pokemon pokemon) {
        Pokemon created = pokemonService.createPokemon(
                pokemon.getPokedexNumber(),
                pokemon.getName(),
                pokemon.getHp(),
                pokemon.getAttack(),
                pokemon.getDefense(),
                pokemon.getSpeed()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    public Response getAllPokemons() {
        List<Pokemon> pokemons = pokemonService.findAllPokemons();
        return Response.ok(pokemons).build();
    }

    @GET
    @Path("/{id}")
    public Response getPokemonById(@PathParam("id") Long id) {
        Pokemon pokemon = pokemonService.findPokemonById(id);
        if (pokemon == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(pokemon).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePokemon(@PathParam("id") Long id, Pokemon pokemon) {
        Pokemon updated = pokemonService.updatePokemon(
                id,
                pokemon.getPokedexNumber(),
                pokemon.getName(),
                pokemon.getHp(),
                pokemon.getAttack(),
                pokemon.getDefense(),
                pokemon.getSpeed()
        );
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePokemon(@PathParam("id") Long id) {
        pokemonService.deletePokemon(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/compare")
    public Response comparePokemons(List<Long> pokemonIds) {
        try {
            PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);
            return Response.ok(comparison).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        }
    }
}

