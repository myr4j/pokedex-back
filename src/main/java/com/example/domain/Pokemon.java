package com.example.domain;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemons")
@Getter
@Setter
@NoArgsConstructor
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer pokedexNumber;

    @Column(nullable = false)
    private String name;

    private Integer hp;
    private Integer attack;
    private Integer defense;
    private Integer speed;

    @ManyToMany
    @JoinTable(
        name = "pokemon_types",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<Type> types = new ArrayList<>();

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonbTransient
    private List<CaughtPokemon> captures = new ArrayList<>();

    public Pokemon(Integer pokedexNumber, String name) {
        this.pokedexNumber = pokedexNumber;
        this.name = name;
    }
}
