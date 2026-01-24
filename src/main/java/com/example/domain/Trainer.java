package com.example.domain;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor
public class Trainer {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonbTransient
    private String password;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonbTransient
    private List<CaughtPokemon> captures = new ArrayList<>();

    public Trainer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Trainer(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
