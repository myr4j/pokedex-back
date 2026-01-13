package com.example.rest;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import com.example.service.CaughtPokemonService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaughtPokemonResourceTest {

    @Mock
    private CaughtPokemonService caughtPokemonService;

    @InjectMocks
    private CaughtPokemonResource caughtPokemonResource;

    @Test
    void testCreateCaughtPokemon() {
        // given
        Map<String, Long> request = new HashMap<>();
        request.put("trainerId", 1L);
        request.put("pokemonId", 2L);

        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(1L);
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        pokemon.setId(2L);
        CaughtPokemon caughtPokemon = new CaughtPokemon(trainer, pokemon);
        caughtPokemon.setId(1L);

        when(caughtPokemonService.createCaughtPokemon(1L, 2L)).thenReturn(caughtPokemon);

        // when
        Response response = caughtPokemonResource.createCaughtPokemon(request);

        // then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(caughtPokemon, response.getEntity());
        verify(caughtPokemonService, times(1)).createCaughtPokemon(1L, 2L);
    }

    @Test
    void testCreateCaughtPokemonWithMissingTrainerId() {
        // given
        Map<String, Long> request = new HashMap<>();
        request.put("pokemonId", 2L);
        // trainerId manquant

        // when
        Response response = caughtPokemonResource.createCaughtPokemon(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("trainerId and pokemonId are required", response.getEntity());
        verify(caughtPokemonService, never()).createCaughtPokemon(any(), any());
    }

    @Test
    void testCreateCaughtPokemonWithMissingPokemonId() {
        // given
        Map<String, Long> request = new HashMap<>();
        request.put("trainerId", 1L);
        // pokemonId manquant

        // when
        Response response = caughtPokemonResource.createCaughtPokemon(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("trainerId and pokemonId are required", response.getEntity());
        verify(caughtPokemonService, never()).createCaughtPokemon(any(), any());
    }

    @Test
    void testCreateCaughtPokemonWithTrainerNotFound() {
        // given
        Map<String, Long> request = new HashMap<>();
        request.put("trainerId", 999L);
        request.put("pokemonId", 2L);

        when(caughtPokemonService.createCaughtPokemon(999L, 2L))
                .thenThrow(new IllegalArgumentException("Trainer not found with id: 999"));

        // when
        Response response = caughtPokemonResource.createCaughtPokemon(request);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Trainer not found with id: 999", response.getEntity());
        verify(caughtPokemonService, times(1)).createCaughtPokemon(999L, 2L);
    }

    @Test
    void testCreateCaughtPokemonWithPokemonNotFound() {
        // given
        Map<String, Long> request = new HashMap<>();
        request.put("trainerId", 1L);
        request.put("pokemonId", 999L);

        when(caughtPokemonService.createCaughtPokemon(1L, 999L))
                .thenThrow(new IllegalArgumentException("Pokemon not found with id: 999"));

        // when
        Response response = caughtPokemonResource.createCaughtPokemon(request);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Pokemon not found with id: 999", response.getEntity());
        verify(caughtPokemonService, times(1)).createCaughtPokemon(1L, 999L);
    }

    @Test
    void testGetAllCaughtPokemons() {
        // given
        List<CaughtPokemon> caughtPokemons = new ArrayList<>();
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        caughtPokemons.add(new CaughtPokemon(trainer, pokemon));

        when(caughtPokemonService.findAllCaughtPokemons()).thenReturn(caughtPokemons);

        // when
        Response response = caughtPokemonResource.getAllCaughtPokemons();

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(caughtPokemons, response.getEntity());
        verify(caughtPokemonService, times(1)).findAllCaughtPokemons();
    }

    @Test
    void testGetCaughtPokemonById() {
        // given
        Long id = 1L;
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        CaughtPokemon caughtPokemon = new CaughtPokemon(trainer, pokemon);
        caughtPokemon.setId(id);

        when(caughtPokemonService.findCaughtPokemonById(id)).thenReturn(caughtPokemon);

        // when
        Response response = caughtPokemonResource.getCaughtPokemonById(id);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(caughtPokemon, response.getEntity());
        verify(caughtPokemonService, times(1)).findCaughtPokemonById(id);
    }

    @Test
    void testGetCaughtPokemonByIdNotFound() {
        // given
        Long id = 999L;

        when(caughtPokemonService.findCaughtPokemonById(id)).thenReturn(null);

        // when
        Response response = caughtPokemonResource.getCaughtPokemonById(id);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(caughtPokemonService, times(1)).findCaughtPokemonById(id);
    }

    @Test
    void testGetCaughtPokemonsByTrainer() {
        // given
        Long trainerId = 1L;
        List<CaughtPokemon> caughtPokemons = new ArrayList<>();
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        caughtPokemons.add(new CaughtPokemon(trainer, pokemon));

        when(caughtPokemonService.findCaughtPokemonsByTrainer(trainerId))
                .thenReturn(caughtPokemons);

        // when
        Response response = caughtPokemonResource.getCaughtPokemonsByTrainer(trainerId);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(caughtPokemons, response.getEntity());
        verify(caughtPokemonService, times(1)).findCaughtPokemonsByTrainer(trainerId);
    }

    @Test
    void testGetCaughtPokemonsByPokemon() {
        // given
        Long pokemonId = 25L;
        List<CaughtPokemon> caughtPokemons = new ArrayList<>();
        Trainer trainer1 = new Trainer("Ash", "ash@pokemon.com");
        Trainer trainer2 = new Trainer("Red", "red@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        caughtPokemons.add(new CaughtPokemon(trainer1, pokemon));
        caughtPokemons.add(new CaughtPokemon(trainer2, pokemon));

        when(caughtPokemonService.findCaughtPokemonsByPokemon(pokemonId))
                .thenReturn(caughtPokemons);

        // when
        Response response = caughtPokemonResource.getCaughtPokemonsByPokemon(pokemonId);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(caughtPokemons, response.getEntity());
        verify(caughtPokemonService, times(1)).findCaughtPokemonsByPokemon(pokemonId);
    }

    @Test
    void testDeleteCaughtPokemon() {
        // given
        Long id = 1L;
        doNothing().when(caughtPokemonService).deleteCaughtPokemon(id);

        // when
        Response response = caughtPokemonResource.deleteCaughtPokemon(id);

        // then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(caughtPokemonService, times(1)).deleteCaughtPokemon(id);
    }
}
