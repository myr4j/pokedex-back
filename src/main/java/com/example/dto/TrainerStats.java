package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStats {
    private Long trainerId;
    private String trainerName;
    private Long totalCaptures;
    private Long uniquePokemons;
    private Double pokedexCompletionPercentage;
    private Map<String, Long> capturesByType;
    private Long totalPokemonsInPokedex;
}
