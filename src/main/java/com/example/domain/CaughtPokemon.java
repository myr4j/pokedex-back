package com.example.domain;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "caught_pokemons")
@Getter
@Setter
@NoArgsConstructor
public class CaughtPokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    @JsonbTransient
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    @JsonbTransient
    private Pokemon pokemon;

    @Column(nullable = false)
    private LocalDateTime captureDate;

    public CaughtPokemon(Trainer trainer, Pokemon pokemon) {
        this.trainer = trainer;
        this.pokemon = pokemon;
        this.captureDate = LocalDateTime.now();
    }
}


