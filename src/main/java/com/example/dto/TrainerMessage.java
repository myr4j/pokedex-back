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
public class TrainerMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long trainerId;
    private String trainerName;
    private String trainerEmail;
    private LocalDateTime registrationDate;
    
    public TrainerMessage(Long trainerId, String trainerName, String trainerEmail) {
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.trainerEmail = trainerEmail;
        this.registrationDate = LocalDateTime.now();
    }
}
