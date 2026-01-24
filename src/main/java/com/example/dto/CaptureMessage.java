package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaptureMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long trainerId;
    private String trainerName;
    private Long pokemonId;
    private String pokemonName;
    private LocalDateTime captureDate;
    
    public CaptureMessage(Long trainerId, String trainerName, Long pokemonId, String pokemonName) {
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.pokemonId = pokemonId;
        this.pokemonName = pokemonName;
        this.captureDate = LocalDateTime.now();
    }
}
