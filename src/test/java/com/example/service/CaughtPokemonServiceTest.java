package com.example.service;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import com.example.messaging.CaptureMessageProducer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
    private CaptureMessageProducer captureMessageProducer;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<CaughtPokemon> cq;

    @Mock
    private Root<CaughtPokemon> root;

    @Mock
    private Path<Object> idPath;

    @Mock
    private Predicate predicate;

    @Mock
    private Order order;

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
        doNothing().when(captureMessageProducer).sendCaptureMessage(any());

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
        verify(captureMessageProducer, times(1)).sendCaptureMessage(any());
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
    void testFindAllCaughtPokemons() {
        // Given
        List<CaughtPokemon> caughtPokemons = new ArrayList<>();
        Trainer trainer1 = new Trainer("Ash", "ash@pokemon.com");
        Trainer trainer2 = new Trainer("Misty", "misty@pokemon.com");
        Pokemon pokemon1 = new Pokemon(25, "Pikachu");
        Pokemon pokemon2 = new Pokemon(7, "Squirtle");
        
        CaughtPokemon cp1 = new CaughtPokemon(trainer1, pokemon1);
        CaughtPokemon cp2 = new CaughtPokemon(trainer2, pokemon2);
        caughtPokemons.add(cp1);
        caughtPokemons.add(cp2);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(CaughtPokemon.class)).thenReturn(cq);
        when(cq.from(CaughtPokemon.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(root.get(anyString())).thenReturn(idPath);
        when(cb.desc(any())).thenReturn(order);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(caughtPokemons);

        // When
        List<CaughtPokemon> result = caughtPokemonService.findAllCaughtPokemons();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(CaughtPokemon.class);
        verify(cq, times(1)).from(CaughtPokemon.class);
        verify(cq, times(1)).select(root);
        verify(root, times(1)).get("captureDate");
        verify(cb, times(1)).desc(idPath);
        verify(cq, times(1)).orderBy(order);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testFindCaughtPokemonsByTrainer() {
        // Given
        Long trainerId = 1L;
        List<CaughtPokemon> caughtPokemons = new ArrayList<>();
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(trainerId);
        Pokemon pokemon1 = new Pokemon(25, "Pikachu");
        Pokemon pokemon2 = new Pokemon(1, "Bulbasaur");
        
        CaughtPokemon cp1 = new CaughtPokemon(trainer, pokemon1);
        CaughtPokemon cp2 = new CaughtPokemon(trainer, pokemon2);
        caughtPokemons.add(cp1);
        caughtPokemons.add(cp2);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(CaughtPokemon.class)).thenReturn(cq);
        when(cq.from(CaughtPokemon.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(idPath);
        when(cb.equal(any(), eq(trainerId))).thenReturn(predicate);
        when(cq.where(any(Predicate.class))).thenReturn(cq);
        when(cb.desc(any())).thenReturn(order);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(caughtPokemons);

        // When
        List<CaughtPokemon> result = caughtPokemonService.findCaughtPokemonsByTrainer(trainerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(CaughtPokemon.class);
        verify(cq, times(1)).from(CaughtPokemon.class);
        verify(root, atLeastOnce()).get(anyString());
        verify(cb, times(1)).equal(any(), eq(trainerId));
        verify(cq, times(1)).where(any(Predicate.class));
        verify(cb, times(1)).desc(any());
        verify(cq, times(1)).orderBy(any(Order.class));
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testFindCaughtPokemonsByPokemon() {
        // Given
        Long pokemonId = 25L;
        List<CaughtPokemon> caughtPokemons = new ArrayList<>();
        Trainer trainer1 = new Trainer("Ash", "ash@pokemon.com");
        Trainer trainer2 = new Trainer("Red", "red@pokemon.com");
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        pokemon.setId(pokemonId);
        
        CaughtPokemon cp1 = new CaughtPokemon(trainer1, pokemon);
        CaughtPokemon cp2 = new CaughtPokemon(trainer2, pokemon);
        caughtPokemons.add(cp1);
        caughtPokemons.add(cp2);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(CaughtPokemon.class)).thenReturn(cq);
        when(cq.from(CaughtPokemon.class)).thenReturn(root);
        when(root.get(anyString())).thenReturn(idPath);
        when(cb.equal(any(), eq(pokemonId))).thenReturn(predicate);
        when(cq.where(any(Predicate.class))).thenReturn(cq);
        when(cb.desc(any())).thenReturn(order);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(caughtPokemons);

        // When
        List<CaughtPokemon> result = caughtPokemonService.findCaughtPokemonsByPokemon(pokemonId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(CaughtPokemon.class);
        verify(cq, times(1)).from(CaughtPokemon.class);
        verify(root, atLeastOnce()).get(anyString());
        verify(cb, times(1)).equal(any(), eq(pokemonId));
        verify(cq, times(1)).where(any(Predicate.class));
        verify(cb, times(1)).desc(any());
        verify(cq, times(1)).orderBy(any(Order.class));
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
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

