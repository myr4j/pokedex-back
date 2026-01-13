package com.example.service;

import com.example.domain.Pokemon;
import com.example.dto.PokemonComparison;
import com.example.dto.PokemonDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class PokemonComparisonService {

    @PersistenceContext
    private EntityManager em;

    public PokemonComparison comparePokemons(List<Long> pokemonIds) {
        if (pokemonIds == null || pokemonIds.isEmpty()) {
            throw new IllegalArgumentException("At least one Pokemon ID is required");
        }

        if (pokemonIds.size() > 10) {
            throw new IllegalArgumentException("Cannot compare more than 10 Pokemon at once");
        }

        // récupérer les pokémon
        List<Pokemon> pokemons = pokemonIds.stream()
                .map(id -> em.find(Pokemon.class, id))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        if (pokemons.isEmpty()) {
            throw new IllegalArgumentException("No valid Pokemon found with the provided IDs");
        }

        // convertir en dtos
        List<PokemonDTO> pokemonDTOs = pokemons.stream()
                .map(PokemonDTO::from)
                .collect(Collectors.toList());

        // calculer les stats comparatives (min, max, moyenne)
        PokemonComparison.ComparisonStats stats = calculateComparisonStats(pokemons);

        PokemonComparison comparison = new PokemonComparison();
        comparison.setPokemons(pokemonDTOs);
        comparison.setStats(stats);

        return comparison;
    }

    private PokemonComparison.ComparisonStats calculateComparisonStats(List<Pokemon> pokemons) {
        PokemonComparison.ComparisonStats stats = new PokemonComparison.ComparisonStats();

        // hp
        List<Integer> hpValues = pokemons.stream()
                .map(Pokemon::getHp)
                .filter(hp -> hp != null)
                .collect(Collectors.toList());
        
        if (!hpValues.isEmpty()) {
            stats.setMinHp(hpValues.stream().min(Integer::compareTo).orElse(0));
            stats.setMaxHp(hpValues.stream().max(Integer::compareTo).orElse(0));
            stats.setAvgHp(hpValues.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        }

        // attack
        List<Integer> attackValues = pokemons.stream()
                .map(Pokemon::getAttack)
                .filter(attack -> attack != null)
                .collect(Collectors.toList());
        
        if (!attackValues.isEmpty()) {
            stats.setMinAttack(attackValues.stream().min(Integer::compareTo).orElse(0));
            stats.setMaxAttack(attackValues.stream().max(Integer::compareTo).orElse(0));
            stats.setAvgAttack(attackValues.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        }

        // defense
        List<Integer> defenseValues = pokemons.stream()
                .map(Pokemon::getDefense)
                .filter(defense -> defense != null)
                .collect(Collectors.toList());
        
        if (!defenseValues.isEmpty()) {
            stats.setMinDefense(defenseValues.stream().min(Integer::compareTo).orElse(0));
            stats.setMaxDefense(defenseValues.stream().max(Integer::compareTo).orElse(0));
            stats.setAvgDefense(defenseValues.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        }

        // speed
        List<Integer> speedValues = pokemons.stream()
                .map(Pokemon::getSpeed)
                .filter(speed -> speed != null)
                .collect(Collectors.toList());
        
        if (!speedValues.isEmpty()) {
            stats.setMinSpeed(speedValues.stream().min(Integer::compareTo).orElse(0));
            stats.setMaxSpeed(speedValues.stream().max(Integer::compareTo).orElse(0));
            stats.setAvgSpeed(speedValues.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        }

        return stats;
    }
}
