package com.example.service;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
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
class CaughtPokemonServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<CaughtPokemon> typedQuery;

    @InjectMocks
    private CaughtPokemonService caughtPokemonService;

    @Test
    void testCreateCaughtPokemon() {
        // Given
        Long trainerId = 1L;
        Long pokemonId = 2L;
        
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(trainerId);
        
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        pokemon.setId(pokemonId);

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(em.find(Pokemon.class, pokemonId)).thenReturn(pokemon);
        
        doAnswer(invocation -> {
            CaughtPokemon cp = invocation.getArgument(0);
            cp.setId(1L);
            return null;
        }).when(em).persist(any(CaughtPokemon.class));

        // When
        CaughtPokemon result = caughtPokemonService.createCaughtPokemon(trainerId, pokemonId);

        // Then
        assertNotNull(result);
        assertEquals(trainer, result.getTrainer());
        assertEquals(pokemon, result.getPokemon());
        assertNotNull(result.getCaptureDate());
        verify(em, times(1)).find(Trainer.class, trainerId);
        verify(em, times(1)).find(Pokemon.class, pokemonId);
        verify(em, times(1)).persist(any(CaughtPokemon.class));
    }

    @Test
    void testCreateCaughtPokemonTrainerNotFound() {
        // Given
        Long trainerId = 999L;
        Long pokemonId = 1L;
        
        when(em.find(Trainer.class, trainerId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            caughtPokemonService.createCaughtPokemon(trainerId, pokemonId);
        });

        assertEquals("Trainer not found with id: " + trainerId, exception.getMessage());
        verify(em, times(1)).find(Trainer.class, trainerId);
        verify(em, never()).persist(any(CaughtPokemon.class));
    }

    @Test
    void testCreateCaughtPokemonPokemonNotFound() {
        // Given
        Long trainerId = 1L;
        Long pokemonId = 999L;
        
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(trainerId);
        
        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(em.find(Pokemon.class, pokemonId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            caughtPokemonService.createCaughtPokemon(trainerId, pokemonId);
        });

        assertEquals("Pokemon not found with id: " + pokemonId, exception.getMessage());
        verify(em, times(1)).find(Trainer.class, trainerId);
        verify(em, times(1)).find(Pokemon.class, pokemonId);
        verify(em, never()).persist(any(CaughtPokemon.class));
    }

    @Test
    void testFindCaughtPokemonById() {
        // Given
        Long id = 1L;
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        CaughtPokemon caughtPokemon = new CaughtPokemon(trainer, pokemon);
        caughtPokemon.setId(id);

        when(em.find(CaughtPokemon.class, id)).thenReturn(caughtPokemon);

        // When
        CaughtPokemon result = caughtPokemonService.findCaughtPokemonById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(trainer, result.getTrainer());
        assertEquals(pokemon, result.getPokemon());
        verify(em, times(1)).find(CaughtPokemon.class, id);
    }

    @Test
    void testFindCaughtPokemonByIdNotFound() {
        // Given
        Long id = 999L;
        when(em.find(CaughtPokemon.class, id)).thenReturn(null);

        // When
        CaughtPokemon result = caughtPokemonService.findCaughtPokemonById(id);

        // Then
        assertNull(result);
        verify(em, times(1)).find(CaughtPokemon.class, id);
    }

    @Test
    void testFindCaughtPokemonsByTrainer() {
        // Given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(trainerId);
        Pokemon pokemon1 = new Pokemon(25, "Pikachu");
        Pokemon pokemon2 = new Pokemon(1, "Bulbasaur");
        
        CaughtPokemon cp1 = new CaughtPokemon(trainer, pokemon1);
        CaughtPokemon cp2 = new CaughtPokemon(trainer, pokemon2);
        trainer.getCaptures().add(cp1);
        trainer.getCaptures().add(cp2);

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);

        // When
        List<CaughtPokemon> result = caughtPokemonService.findCaughtPokemonsByTrainer(trainerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).find(Trainer.class, trainerId);
        verify(em, never()).createQuery(anyString(), any());
    }

    @Test
    void testFindCaughtPokemonsByPokemon() {
        // Given
        Long pokemonId = 25L;
        Trainer trainer1 = new Trainer("Ash", "ash@pokemon.com");
        Trainer trainer2 = new Trainer("Red", "red@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        pokemon.setId(pokemonId);
        
        CaughtPokemon cp1 = new CaughtPokemon(trainer1, pokemon);
        CaughtPokemon cp2 = new CaughtPokemon(trainer2, pokemon);
        pokemon.getCaptures().add(cp1);
        pokemon.getCaptures().add(cp2);

        when(em.find(Pokemon.class, pokemonId)).thenReturn(pokemon);

        // When
        List<CaughtPokemon> result = caughtPokemonService.findCaughtPokemonsByPokemon(pokemonId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).find(Pokemon.class, pokemonId);
        verify(em, never()).createQuery(anyString(), any());
    }

    @Test
    void testDeleteCaughtPokemon() {
        // Given
        Long id = 1L;
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        CaughtPokemon caughtPokemon = new CaughtPokemon(trainer, pokemon);
        caughtPokemon.setId(id);

        when(em.find(CaughtPokemon.class, id)).thenReturn(caughtPokemon);
        doNothing().when(em).remove(any(CaughtPokemon.class));

        // When
        caughtPokemonService.deleteCaughtPokemon(id);

        // Then
        verify(em, times(1)).find(CaughtPokemon.class, id);
        verify(em, times(1)).remove(caughtPokemon);
    }

    @Test
    void testDeleteCaughtPokemonNotFound() {
        // Given
        Long id = 999L;
        when(em.find(CaughtPokemon.class, id)).thenReturn(null);

        // When
        caughtPokemonService.deleteCaughtPokemon(id);

        // Then
        verify(em, times(1)).find(CaughtPokemon.class, id);
        verify(em, never()).remove(any(CaughtPokemon.class));
    }
}

