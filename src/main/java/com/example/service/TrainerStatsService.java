package com.example.service;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import com.example.domain.Type;
import com.example.dto.TrainerStats;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class TrainerStatsService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private CaughtPokemonService caughtPokemonService;

    @Inject
    private PokemonService pokemonService;

    public TrainerStats getTrainerStats(Long trainerId) {
        Trainer trainer = em.find(Trainer.class, trainerId);
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer not found with id: " + trainerId);
        }

        // récupérer toutes les captures du dresseur
        List<CaughtPokemon> captures = caughtPokemonService.findCaughtPokemonsByTrainer(trainerId);

        // calculer les stats
        long totalCaptures = captures.size();

        // pokémon uniques (distincts)
        Set<Long> uniquePokemonIds = captures.stream()
                .map(cp -> cp.getPokemon().getId())
                .collect(Collectors.toSet());
        long uniquePokemons = uniquePokemonIds.size();

        // total de pokémon dans le pokedex
        long totalPokemonsInPokedex = pokemonService.findAllPokemons().size();

        // pourcentage de completion
        double pokedexCompletionPercentage = totalPokemonsInPokedex > 0
                ? (uniquePokemons * 100.0) / totalPokemonsInPokedex
                : 0.0;

        // captures par type (on compte chaque type du pokémon)
        Map<String, Long> capturesByType = new HashMap<>();
        for (CaughtPokemon capture : captures) {
            Pokemon pokemon = capture.getPokemon();
            if (pokemon.getTypes() != null) {
                for (Type type : pokemon.getTypes()) {
                    capturesByType.merge(type.getName(), 1L, Long::sum);
                }
            }
        }

        TrainerStats stats = new TrainerStats();
        stats.setTrainerId(trainer.getId());
        stats.setTrainerName(trainer.getName());
        stats.setTotalCaptures(totalCaptures);
        stats.setUniquePokemons(uniquePokemons);
        stats.setPokedexCompletionPercentage(pokedexCompletionPercentage);
        stats.setCapturesByType(capturesByType);
        stats.setTotalPokemonsInPokedex(totalPokemonsInPokedex);

        return stats;
    }
}
