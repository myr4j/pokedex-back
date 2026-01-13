package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PokemonComparison {
    private List<PokemonDTO> pokemons;
    private ComparisonStats stats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonStats {
        private Integer minHp;
        private Integer maxHp;
        private Double avgHp;
        
        private Integer minAttack;
        private Integer maxAttack;
        private Double avgAttack;
        
        private Integer minDefense;
        private Integer maxDefense;
        private Double avgDefense;
        
        private Integer minSpeed;
        private Integer maxSpeed;
        private Double avgSpeed;
    }
}
