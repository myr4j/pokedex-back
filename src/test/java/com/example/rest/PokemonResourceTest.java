package com.example.rest;

import com.example.domain.Pokemon;
import com.example.dto.PokemonComparison;
import com.example.dto.PokemonDTO;
import com.example.service.PokemonComparisonService;
import com.example.service.PokemonService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonResourceTest {

    @Mock
    private PokemonService pokemonService;

    @Mock
    private PokemonComparisonService pokemonComparisonService;

    @InjectMocks
    private PokemonResource pokemonResource;

    @Test
    void testCreatePokemon() {
        // given
        Pokemon pokemon = new Pokemon(1, "Bulbasaur");
        pokemon.setHp(45);
        pokemon.setAttack(49);
        pokemon.setDefense(49);
        pokemon.setSpeed(45);

        Pokemon created = new Pokemon(1, "Bulbasaur");
        created.setId(1L);
        created.setHp(45);
        created.setAttack(49);
        created.setDefense(49);
        created.setSpeed(45);

        when(pokemonService.createPokemon(1, "Bulbasaur", 45, 49, 49, 45))
                .thenReturn(created);

        // when
        Response response = pokemonResource.createPokemon(pokemon);

        // then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(created, response.getEntity());
        verify(pokemonService, times(1))
                .createPokemon(1, "Bulbasaur", 45, 49, 49, 45);
    }

    @Test
    void testGetAllPokemons() {
        // given
        List<Pokemon> pokemons = new ArrayList<>();
        pokemons.add(new Pokemon(1, "Bulbasaur"));
        pokemons.add(new Pokemon(4, "Charmander"));

        when(pokemonService.findAllPokemons()).thenReturn(pokemons);

        // when
        Response response = pokemonResource.getAllPokemons();

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(pokemons, response.getEntity());
        verify(pokemonService, times(1)).findAllPokemons();
    }

    @Test
    void testGetPokemonById() {
        // given
        Long id = 1L;
        Pokemon pokemon = new Pokemon(1, "Bulbasaur");
        pokemon.setId(id);
        pokemon.setHp(45);
        pokemon.setAttack(49);

        when(pokemonService.findPokemonById(id)).thenReturn(pokemon);

        // when
        Response response = pokemonResource.getPokemonById(id);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(pokemon, response.getEntity());
        verify(pokemonService, times(1)).findPokemonById(id);
    }

    @Test
    void testGetPokemonByIdNotFound() {
        // given
        Long id = 999L;

        when(pokemonService.findPokemonById(id)).thenReturn(null);

        // when
        Response response = pokemonResource.getPokemonById(id);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(pokemonService, times(1)).findPokemonById(id);
    }

    @Test
    void testUpdatePokemon() {
        // given
        Long id = 1L;
        Pokemon pokemon = new Pokemon(1, "Bulbasaur Updated");
        pokemon.setHp(50);
        pokemon.setAttack(55);
        pokemon.setDefense(50);
        pokemon.setSpeed(50);

        Pokemon updated = new Pokemon(1, "Bulbasaur Updated");
        updated.setId(id);
        updated.setHp(50);
        updated.setAttack(55);
        updated.setDefense(50);
        updated.setSpeed(50);

        when(pokemonService.updatePokemon(id, 1, "Bulbasaur Updated", 50, 55, 50, 50))
                .thenReturn(updated);

        // when
        Response response = pokemonResource.updatePokemon(id, pokemon);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(updated, response.getEntity());
        verify(pokemonService, times(1))
                .updatePokemon(id, 1, "Bulbasaur Updated", 50, 55, 50, 50);
    }

    @Test
    void testUpdatePokemonNotFound() {
        // given
        Long id = 999L;
        Pokemon pokemon = new Pokemon(999, "Unknown");
        pokemon.setHp(50);

        when(pokemonService.updatePokemon(id, 999, "Unknown", 50, null, null, null))
                .thenReturn(null);

        // when
        Response response = pokemonResource.updatePokemon(id, pokemon);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(pokemonService, times(1))
                .updatePokemon(id, 999, "Unknown", 50, null, null, null);
    }

    @Test
    void testDeletePokemon() {
        // given
        Long id = 1L;
        doNothing().when(pokemonService).deletePokemon(id);

        // when
        Response response = pokemonResource.deletePokemon(id);

        // then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(pokemonService, times(1)).deletePokemon(id);
    }

    @Test
    void testComparePokemons() {
        // given
        List<Long> pokemonIds = List.of(1L, 2L);
        
        PokemonComparison comparison = new PokemonComparison();
        PokemonComparison.ComparisonStats stats = new PokemonComparison.ComparisonStats();
        stats.setMinHp(39);
        stats.setMaxHp(45);
        stats.setAvgHp(42.0);
        comparison.setStats(stats);
        comparison.setPokemons(new ArrayList<>());

        when(pokemonComparisonService.comparePokemons(pokemonIds)).thenReturn(comparison);

        // when
        Response response = pokemonResource.comparePokemons(pokemonIds);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(comparison, response.getEntity());
        verify(pokemonComparisonService, times(1)).comparePokemons(pokemonIds);
    }

    @Test
    void testComparePokemonsWithBadRequest() {
        // given
        List<Long> pokemonIds = new ArrayList<>();

        when(pokemonComparisonService.comparePokemons(pokemonIds))
                .thenThrow(new IllegalArgumentException("At least one Pokemon ID is required"));

        // when
        Response response = pokemonResource.comparePokemons(pokemonIds);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("At least one Pokemon ID is required", response.getEntity());
        verify(pokemonComparisonService, times(1)).comparePokemons(pokemonIds);
    }

    @Test
    void testComparePokemonsWithTooManyPokemons() {
        // given
        List<Long> pokemonIds = new ArrayList<>();
        for (long i = 1; i <= 11; i++) {
            pokemonIds.add(i);
        }

        when(pokemonComparisonService.comparePokemons(pokemonIds))
                .thenThrow(new IllegalArgumentException("Cannot compare more than 10 Pokemon at once"));

        // when
        Response response = pokemonResource.comparePokemons(pokemonIds);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Cannot compare more than 10 Pokemon at once", response.getEntity());
        verify(pokemonComparisonService, times(1)).comparePokemons(pokemonIds);
    }
}
