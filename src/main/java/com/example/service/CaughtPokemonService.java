package com.example.service;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class CaughtPokemonService {

    @PersistenceContext
    private EntityManager em;

    public CaughtPokemon createCaughtPokemon(Long trainerId, Long pokemonId) {
        Trainer trainer = em.find(Trainer.class, trainerId);
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer not found with id: " + trainerId);
        }
        
        Pokemon pokemon = em.find(Pokemon.class, pokemonId);
        if (pokemon == null) {
            throw new IllegalArgumentException("Pokemon not found with id: " + pokemonId);
        }
        
        CaughtPokemon caughtPokemon = new CaughtPokemon(trainer, pokemon);
        em.persist(caughtPokemon);
        return caughtPokemon;
    }

    public CaughtPokemon findCaughtPokemonById(Long id) {
        return em.find(CaughtPokemon.class, id);
    }


    public List<CaughtPokemon> findCaughtPokemonsByTrainer(Long trainerId) {
        Trainer trainer = em.find(Trainer.class, trainerId);
        if (trainer != null && trainer.getCaptures() != null) {
            return trainer.getCaptures();
        }
        return new java.util.ArrayList<>();
    }

    public List<CaughtPokemon> findCaughtPokemonsByPokemon(Long pokemonId) {
        Pokemon pokemon = em.find(Pokemon.class, pokemonId);
        if (pokemon != null && pokemon.getCaptures() != null) {
            return pokemon.getCaptures();
        }
        return new java.util.ArrayList<>();
    }

    public void deleteCaughtPokemon(Long id) {
        CaughtPokemon caughtPokemon = findCaughtPokemonById(id);
        if (caughtPokemon != null) {
            em.remove(caughtPokemon);
        }
    }
}

