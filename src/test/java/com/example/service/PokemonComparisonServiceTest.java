package com.example.service;

import com.example.domain.Pokemon;
import com.example.dto.PokemonComparison;
import com.example.dto.PokemonDTO;
import jakarta.persistence.EntityManager;
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
class PokemonComparisonServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private PokemonComparisonService pokemonComparisonService;

    @Test
    void testComparePokemonsWithTwoPokemons() {
        // given
        Pokemon pokemon1 = new Pokemon(1, "Bulbasaur");
        pokemon1.setId(1L);
        pokemon1.setHp(45);
        pokemon1.setAttack(49);
        pokemon1.setDefense(49);
        pokemon1.setSpeed(45);

        Pokemon pokemon2 = new Pokemon(4, "Charmander");
        pokemon2.setId(2L);
        pokemon2.setHp(39);
        pokemon2.setAttack(52);
        pokemon2.setDefense(43);
        pokemon2.setSpeed(65);

        List<Long> pokemonIds = List.of(1L, 2L);

        when(em.find(Pokemon.class, 1L)).thenReturn(pokemon1);
        when(em.find(Pokemon.class, 2L)).thenReturn(pokemon2);

        // when
        PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);

        // then
        assertNotNull(comparison);
        assertNotNull(comparison.getPokemons());
        assertEquals(2, comparison.getPokemons().size());
        assertNotNull(comparison.getStats());

        PokemonComparison.ComparisonStats stats = comparison.getStats();
        assertEquals(39, stats.getMinHp());
        assertEquals(45, stats.getMaxHp());
        assertEquals(42.0, stats.getAvgHp(), 0.01);

        assertEquals(49, stats.getMinAttack());
        assertEquals(52, stats.getMaxAttack());
        assertEquals(50.5, stats.getAvgAttack(), 0.01);

        assertEquals(43, stats.getMinDefense());
        assertEquals(49, stats.getMaxDefense());
        assertEquals(46.0, stats.getAvgDefense(), 0.01);

        assertEquals(45, stats.getMinSpeed());
        assertEquals(65, stats.getMaxSpeed());
        assertEquals(55.0, stats.getAvgSpeed(), 0.01);

        verify(em, times(1)).find(Pokemon.class, 1L);
        verify(em, times(1)).find(Pokemon.class, 2L);
    }

    @Test
    void testComparePokemonsWithThreePokemons() {
        // given
        Pokemon pokemon1 = new Pokemon(1, "Bulbasaur");
        pokemon1.setId(1L);
        pokemon1.setHp(45);
        pokemon1.setAttack(49);
        pokemon1.setDefense(49);
        pokemon1.setSpeed(45);

        Pokemon pokemon2 = new Pokemon(4, "Charmander");
        pokemon2.setId(2L);
        pokemon2.setHp(39);
        pokemon2.setAttack(52);
        pokemon2.setDefense(43);
        pokemon2.setSpeed(65);

        Pokemon pokemon3 = new Pokemon(7, "Squirtle");
        pokemon3.setId(3L);
        pokemon3.setHp(44);
        pokemon3.setAttack(48);
        pokemon3.setDefense(65);
        pokemon3.setSpeed(43);

        List<Long> pokemonIds = List.of(1L, 2L, 3L);

        when(em.find(Pokemon.class, 1L)).thenReturn(pokemon1);
        when(em.find(Pokemon.class, 2L)).thenReturn(pokemon2);
        when(em.find(Pokemon.class, 3L)).thenReturn(pokemon3);

        // when
        PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);

        // then
        assertNotNull(comparison);
        assertEquals(3, comparison.getPokemons().size());

        PokemonComparison.ComparisonStats stats = comparison.getStats();
        assertEquals(39, stats.getMinHp());
        assertEquals(45, stats.getMaxHp());
        assertEquals(42.67, stats.getAvgHp(), 0.01);
    }

    @Test
    void testComparePokemonsWithNullValues() {
        // given
        Pokemon pokemon1 = new Pokemon(1, "Bulbasaur");
        pokemon1.setId(1L);
        pokemon1.setHp(45);
        pokemon1.setAttack(null); // null
        pokemon1.setDefense(49);
        pokemon1.setSpeed(null); // null

        Pokemon pokemon2 = new Pokemon(4, "Charmander");
        pokemon2.setId(2L);
        pokemon2.setHp(39);
        pokemon2.setAttack(52);
        pokemon2.setDefense(43);
        pokemon2.setSpeed(65);

        List<Long> pokemonIds = List.of(1L, 2L);

        when(em.find(Pokemon.class, 1L)).thenReturn(pokemon1);
        when(em.find(Pokemon.class, 2L)).thenReturn(pokemon2);

        // when
        PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);

        // then
        assertNotNull(comparison);
        PokemonComparison.ComparisonStats stats = comparison.getStats();
        
        // hp devrait être calculé (les deux ont des valeurs)
        assertEquals(39, stats.getMinHp());
        assertEquals(45, stats.getMaxHp());
        
        // attack devrait être calculé (seulement pokemon2 a une valeur)
        assertEquals(52, stats.getMinAttack());
        assertEquals(52, stats.getMaxAttack());
        
        // defense devrait être calculé (les deux ont des valeurs)
        assertEquals(43, stats.getMinDefense());
        assertEquals(49, stats.getMaxDefense());
        
        // speed devrait être calculé (seulement pokemon2 a une valeur)
        assertEquals(65, stats.getMinSpeed());
        assertEquals(65, stats.getMaxSpeed());
    }

    @Test
    void testComparePokemonsWithSinglePokemon() {
        // given
        Pokemon pokemon = new Pokemon(25, "Pikachu");
        pokemon.setId(1L);
        pokemon.setHp(35);
        pokemon.setAttack(55);
        pokemon.setDefense(30);
        pokemon.setSpeed(90);

        List<Long> pokemonIds = List.of(1L);

        when(em.find(Pokemon.class, 1L)).thenReturn(pokemon);

        // when
        PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);

        // then
        assertNotNull(comparison);
        assertEquals(1, comparison.getPokemons().size());

        PokemonComparison.ComparisonStats stats = comparison.getStats();
        assertEquals(35, stats.getMinHp());
        assertEquals(35, stats.getMaxHp());
        assertEquals(35.0, stats.getAvgHp());
        assertEquals(55, stats.getMinAttack());
        assertEquals(55, stats.getMaxAttack());
        assertEquals(55.0, stats.getAvgAttack());
    }

    @Test
    void testComparePokemonsWithMaxLimit() {
        // given
        List<Long> pokemonIds = new ArrayList<>();
        for (long i = 1; i <= 11; i++) {
            pokemonIds.add(i);
        }

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pokemonComparisonService.comparePokemons(pokemonIds);
        });

        assertEquals("Cannot compare more than 10 Pokemon at once", exception.getMessage());
        verify(em, never()).find(any(), any());
    }

    @Test
    void testComparePokemonsWithEmptyList() {
        // given
        List<Long> emptyList = new ArrayList<>();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pokemonComparisonService.comparePokemons(emptyList);
        });

        assertEquals("At least one Pokemon ID is required", exception.getMessage());
        verify(em, never()).find(any(), any());
    }

    @Test
    void testComparePokemonsWithNullList() {
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pokemonComparisonService.comparePokemons(null);
        });

        assertEquals("At least one Pokemon ID is required", exception.getMessage());
        verify(em, never()).find(any(), any());
    }

    @Test
    void testComparePokemonsWithInvalidIds() {
        // given
        List<Long> pokemonIds = List.of(999L, 1000L);

        when(em.find(Pokemon.class, 999L)).thenReturn(null);
        when(em.find(Pokemon.class, 1000L)).thenReturn(null);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pokemonComparisonService.comparePokemons(pokemonIds);
        });

        assertEquals("No valid Pokemon found with the provided IDs", exception.getMessage());
        verify(em, times(1)).find(Pokemon.class, 999L);
        verify(em, times(1)).find(Pokemon.class, 1000L);
    }

    @Test
    void testComparePokemonsWithMixedValidAndInvalidIds() {
        // given
        Pokemon validPokemon = new Pokemon(1, "Bulbasaur");
        validPokemon.setId(1L);
        validPokemon.setHp(45);
        validPokemon.setAttack(49);
        validPokemon.setDefense(49);
        validPokemon.setSpeed(45);

        List<Long> pokemonIds = List.of(1L, 999L);

        when(em.find(Pokemon.class, 1L)).thenReturn(validPokemon);
        when(em.find(Pokemon.class, 999L)).thenReturn(null);

        // when
        PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);

        // then
        assertNotNull(comparison);
        assertEquals(1, comparison.getPokemons().size());
        assertEquals("Bulbasaur", comparison.getPokemons().get(0).getName());
    }

    @Test
    void testComparePokemonsWithTenPokemons() {
        // given
        List<Long> pokemonIds = new ArrayList<>();
        List<Pokemon> pokemons = new ArrayList<>();
        
        for (long i = 1; i <= 10; i++) {
            pokemonIds.add(i);
            Pokemon pokemon = new Pokemon((int) i, "Pokemon" + i);
            pokemon.setId(i);
            pokemon.setHp(40 + (int) i);
            pokemon.setAttack(50 + (int) i);
            pokemon.setDefense(45 + (int) i);
            pokemon.setSpeed(35 + (int) i);
            pokemons.add(pokemon);
            
            when(em.find(Pokemon.class, i)).thenReturn(pokemon);
        }

        // when
        PokemonComparison comparison = pokemonComparisonService.comparePokemons(pokemonIds);

        // then
        assertNotNull(comparison);
        assertEquals(10, comparison.getPokemons().size());
        
        PokemonComparison.ComparisonStats stats = comparison.getStats();
        assertEquals(41, stats.getMinHp()); // 40 + 1
        assertEquals(50, stats.getMaxHp()); // 40 + 10
    }
}
