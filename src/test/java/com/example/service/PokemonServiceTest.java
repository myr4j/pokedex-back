package com.example.service;

import com.example.domain.Pokemon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
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
class PokemonServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<Pokemon> cq;

    @Mock
    private Root<Pokemon> root;

    @Mock
    private Path<Object> idPath;

    @Mock
    private Order order;

    @Mock
    private TypedQuery<Pokemon> typedQuery;

    @InjectMocks
    private PokemonService pokemonService;

    @Test
    void testCreatePokemon() {
        // given
        Integer pokedexNumber = 1;
        String name = "Bulbasaur";
        Integer hp = 45;
        Integer attack = 49;
        Integer defense = 49;
        Integer speed = 45;

        doAnswer(invocation -> {
            Pokemon p = invocation.getArgument(0);
            p.setId(1L);
            return null;
        }).when(em).persist(any(Pokemon.class));

        //when
        Pokemon result = pokemonService.createPokemon(pokedexNumber, name, hp, attack, defense, speed);

        // then
        assertNotNull(result);
        assertEquals(pokedexNumber, result.getPokedexNumber());
        assertEquals(name, result.getName());
        assertEquals(hp, result.getHp());
        assertEquals(attack, result.getAttack());
        assertEquals(defense, result.getDefense());
        assertEquals(speed, result.getSpeed());
        verify(em, times(1)).persist(any(Pokemon.class));
    }

    @Test
    void testFindPokemonById() {
        // given
        Long id = 1L;
        Pokemon pokemon = new Pokemon(2, "Ivysaur");
        pokemon.setId(id);
        pokemon.setHp(60);
        pokemon.setAttack(62);
        pokemon.setDefense(63);
        pokemon.setSpeed(60);

        when(em.find(Pokemon.class, id)).thenReturn(pokemon);

        // when
        Pokemon result = pokemonService.findPokemonById(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(2, result.getPokedexNumber());
        assertEquals("Ivysaur", result.getName());
        verify(em, times(1)).find(Pokemon.class, id);
    }

    @Test
    void testFindAllPokemons() {
        // given
        List<Pokemon> pokemons = new ArrayList<>();
        Pokemon p1 = new Pokemon(3, "Venusaur");
        Pokemon p2 = new Pokemon(4, "Charmander");
        pokemons.add(p1);
        pokemons.add(p2);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Pokemon.class)).thenReturn(cq);
        when(cq.from(Pokemon.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(root.get(anyString())).thenReturn(idPath);
        when(cb.asc(any())).thenReturn(order);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(pokemons);

        // when
        List<Pokemon> result = pokemonService.findAllPokemons();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Pokemon.class);
        verify(cq, times(1)).from(Pokemon.class);
        verify(cq, times(1)).select(root);
        verify(cb, times(1)).asc(any());
        verify(cq, times(1)).orderBy(any(Order.class));
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testUpdatePokemon() {
        // Given
        Long id = 1L;
        Pokemon existingPokemon = new Pokemon(5, "Charmeleon");
        existingPokemon.setId(id);
        existingPokemon.setHp(58);
        existingPokemon.setAttack(64);
        existingPokemon.setDefense(58);
        existingPokemon.setSpeed(80);

        when(em.find(Pokemon.class, id)).thenReturn(existingPokemon);
        when(em.merge(any(Pokemon.class))).thenReturn(existingPokemon);

        // When
        Pokemon result = pokemonService.updatePokemon(id, 6, "Charizard", 78, 84, 78, 100);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(6, result.getPokedexNumber());
        assertEquals("Charizard", result.getName());
        assertEquals(78, result.getHp());
        assertEquals(84, result.getAttack());
        verify(em, times(1)).find(Pokemon.class, id);
        verify(em, times(1)).merge(existingPokemon);
    }

    @Test
    void testDeletePokemon() {
        // Given
        Long id = 1L;
        Pokemon pokemon = new Pokemon(7, "Squirtle");
        pokemon.setId(id);

        when(em.find(Pokemon.class, id)).thenReturn(pokemon);
        doNothing().when(em).remove(any(Pokemon.class));

        // When
        pokemonService.deletePokemon(id);

        // Then
        verify(em, times(1)).find(Pokemon.class, id);
        verify(em, times(1)).remove(pokemon);
    }

    @Test
    void testDeletePokemonNotFound() {
        // Given
        Long id = 999L;
        when(em.find(Pokemon.class, id)).thenReturn(null);

        // When
        pokemonService.deletePokemon(id);

        // Then
        verify(em, times(1)).find(Pokemon.class, id);
        verify(em, never()).remove(any(Pokemon.class));
    }
}
