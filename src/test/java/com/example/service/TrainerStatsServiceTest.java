package com.example.service;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import com.example.domain.Type;
import com.example.dto.TrainerStats;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerStatsServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private CaughtPokemonService caughtPokemonService;

    @Mock
    private PokemonService pokemonService;

    @InjectMocks
    private TrainerStatsService trainerStatsService;

    @Test
    void testGetTrainerStats() {
        // given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(trainerId);

        // créer des pokémon avec types
        Pokemon pokemon1 = new Pokemon(25, "Pikachu");
        pokemon1.setId(1L);
        pokemon1.setHp(35);
        pokemon1.setAttack(55);
        
        Pokemon pokemon2 = new Pokemon(1, "Bulbasaur");
        pokemon2.setId(2L);
        pokemon2.setHp(45);
        pokemon2.setAttack(49);
        
        Pokemon pokemon3 = new Pokemon(25, "Pikachu");
        pokemon3.setId(1L); // même pokémon que pokemon1

        // créer des types
        Type fireType = new Type("Fire");
        fireType.setId(1L);
        Type electricType = new Type("Electric");
        electricType.setId(2L);
        Type grassType = new Type("Grass");
        grassType.setId(3L);

        // associer types aux pokémon
        pokemon1.setTypes(List.of(electricType));
        pokemon2.setTypes(List.of(grassType));
        pokemon3.setTypes(List.of(electricType));

        // créer des captures
        CaughtPokemon capture1 = new CaughtPokemon(trainer, pokemon1);
        capture1.setId(1L);
        CaughtPokemon capture2 = new CaughtPokemon(trainer, pokemon2);
        capture2.setId(2L);
        CaughtPokemon capture3 = new CaughtPokemon(trainer, pokemon3);
        capture3.setId(3L);

        List<CaughtPokemon> captures = List.of(capture1, capture2, capture3);

        // créer tous les pokémon du pokedex
        List<Pokemon> allPokemons = List.of(pokemon1, pokemon2, new Pokemon(3, "Venusaur"));

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(caughtPokemonService.findCaughtPokemonsByTrainer(trainerId)).thenReturn(captures);
        when(pokemonService.findAllPokemons()).thenReturn(allPokemons);

        // when
        TrainerStats stats = trainerStatsService.getTrainerStats(trainerId);

        // then
        assertNotNull(stats);
        assertEquals(trainerId, stats.getTrainerId());
        assertEquals("Ash", stats.getTrainerName());
        assertEquals(3L, stats.getTotalCaptures());
        assertEquals(2L, stats.getUniquePokemons()); // pokemon1 et pokemon2 (pokemon3 est dupliqué)
        assertEquals(3L, stats.getTotalPokemonsInPokedex());
        assertEquals(66.67, stats.getPokedexCompletionPercentage(), 0.01); // 2/3 * 100
        
        Map<String, Long> capturesByType = stats.getCapturesByType();
        assertNotNull(capturesByType);
        assertEquals(2L, capturesByType.get("Electric")); // pokemon1 et pokemon3
        assertEquals(1L, capturesByType.get("Grass")); // pokemon2

        verify(em, times(1)).find(Trainer.class, trainerId);
        verify(caughtPokemonService, times(1)).findCaughtPokemonsByTrainer(trainerId);
        verify(pokemonService, times(1)).findAllPokemons();
    }

    @Test
    void testGetTrainerStatsWithNoCaptures() {
        // given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("Misty", "misty@pokemon.com");
        trainer.setId(trainerId);

        List<CaughtPokemon> emptyCaptures = new ArrayList<>();
        List<Pokemon> allPokemons = List.of(
            new Pokemon(1, "Bulbasaur"),
            new Pokemon(2, "Ivysaur")
        );

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(caughtPokemonService.findCaughtPokemonsByTrainer(trainerId)).thenReturn(emptyCaptures);
        when(pokemonService.findAllPokemons()).thenReturn(allPokemons);

        // when
        TrainerStats stats = trainerStatsService.getTrainerStats(trainerId);

        // then
        assertNotNull(stats);
        assertEquals(0L, stats.getTotalCaptures());
        assertEquals(0L, stats.getUniquePokemons());
        assertEquals(0.0, stats.getPokedexCompletionPercentage());
        assertTrue(stats.getCapturesByType().isEmpty());
        assertEquals(2L, stats.getTotalPokemonsInPokedex());
    }

    @Test
    void testGetTrainerStatsWithPokemonHavingMultipleTypes() {
        // given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("Brock", "brock@pokemon.com");
        trainer.setId(trainerId);

        Pokemon pokemon = new Pokemon(1, "Bulbasaur");
        pokemon.setId(1L);
        
        Type grassType = new Type("Grass");
        Type poisonType = new Type("Poison");
        pokemon.setTypes(List.of(grassType, poisonType));

        CaughtPokemon capture = new CaughtPokemon(trainer, pokemon);
        capture.setId(1L);

        List<CaughtPokemon> captures = List.of(capture);
        List<Pokemon> allPokemons = List.of(pokemon);

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(caughtPokemonService.findCaughtPokemonsByTrainer(trainerId)).thenReturn(captures);
        when(pokemonService.findAllPokemons()).thenReturn(allPokemons);

        // when
        TrainerStats stats = trainerStatsService.getTrainerStats(trainerId);

        // then
        assertNotNull(stats);
        Map<String, Long> capturesByType = stats.getCapturesByType();
        assertEquals(1L, capturesByType.get("Grass"));
        assertEquals(1L, capturesByType.get("Poison"));
    }

    @Test
    void testGetTrainerStatsWithPokemonHavingNoTypes() {
        // given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("Gary", "gary@pokemon.com");
        trainer.setId(trainerId);

        Pokemon pokemon = new Pokemon(1, "Bulbasaur");
        pokemon.setId(1L);
        pokemon.setTypes(null); // pas de types

        CaughtPokemon capture = new CaughtPokemon(trainer, pokemon);
        capture.setId(1L);

        List<CaughtPokemon> captures = List.of(capture);
        List<Pokemon> allPokemons = List.of(pokemon);

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(caughtPokemonService.findCaughtPokemonsByTrainer(trainerId)).thenReturn(captures);
        when(pokemonService.findAllPokemons()).thenReturn(allPokemons);

        // when
        TrainerStats stats = trainerStatsService.getTrainerStats(trainerId);

        // then
        assertNotNull(stats);
        assertTrue(stats.getCapturesByType().isEmpty());
    }

    @Test
    void testGetTrainerStatsTrainerNotFound() {
        // given
        Long trainerId = 999L;

        when(em.find(Trainer.class, trainerId)).thenReturn(null);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            trainerStatsService.getTrainerStats(trainerId);
        });

        assertEquals("Trainer not found with id: " + trainerId, exception.getMessage());
        verify(em, times(1)).find(Trainer.class, trainerId);
        verify(caughtPokemonService, never()).findCaughtPokemonsByTrainer(any());
        verify(pokemonService, never()).findAllPokemons();
    }

    @Test
    void testGetTrainerStatsWithEmptyPokedex() {
        // given
        Long trainerId = 1L;
        Trainer trainer = new Trainer("Red", "red@pokemon.com");
        trainer.setId(trainerId);

        Pokemon pokemon = new Pokemon(1, "Bulbasaur");
        pokemon.setId(1L);
        CaughtPokemon capture = new CaughtPokemon(trainer, pokemon);
        capture.setId(1L);

        List<CaughtPokemon> captures = List.of(capture);
        List<Pokemon> emptyPokedex = new ArrayList<>();

        when(em.find(Trainer.class, trainerId)).thenReturn(trainer);
        when(caughtPokemonService.findCaughtPokemonsByTrainer(trainerId)).thenReturn(captures);
        when(pokemonService.findAllPokemons()).thenReturn(emptyPokedex);

        // when
        TrainerStats stats = trainerStatsService.getTrainerStats(trainerId);

        // then
        assertNotNull(stats);
        assertEquals(0.0, stats.getPokedexCompletionPercentage());
        assertEquals(0L, stats.getTotalPokemonsInPokedex());
    }
}
