package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

// message agrege contenant les statistiques de captures groupees par trainer
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedCaptureStats implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long trainerId;
    private String trainerName;
    private Integer totalCaptures;
    private List<PokemonCaptureCount> pokemonCounts; // nombre de captures par pokemon
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PokemonCaptureCount implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long pokemonId;
        private String pokemonName;
        private Integer count;
    }
}
