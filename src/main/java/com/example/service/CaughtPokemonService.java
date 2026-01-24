package com.example.service;

import com.example.domain.CaughtPokemon;
import com.example.domain.Pokemon;
import com.example.domain.Trainer;
import com.example.dto.CaptureMessage;
import com.example.messaging.CaptureMessageProducer;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Stateless
public class CaughtPokemonService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private CaptureMessageProducer captureMessageProducer;

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
        
        // envoyer un message jms pour la capture
        CaptureMessage captureMessage = new CaptureMessage(
                trainer.getId(),
                trainer.getName(),
                pokemon.getId(),
                pokemon.getName()
        );
        captureMessageProducer.sendCaptureMessage(captureMessage);
        
        return caughtPokemon;
    }

    public CaughtPokemon findCaughtPokemonById(Long id) {
        return em.find(CaughtPokemon.class, id);
    }

    public List<CaughtPokemon> findAllCaughtPokemons() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CaughtPokemon> cq = cb.createQuery(CaughtPokemon.class);
        Root<CaughtPokemon> root = cq.from(CaughtPokemon.class);
        cq.select(root);
        cq.orderBy(cb.desc(root.get("captureDate")));
        return em.createQuery(cq).getResultList();
    }

    public List<CaughtPokemon> findCaughtPokemonsByTrainer(Long trainerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CaughtPokemon> cq = cb.createQuery(CaughtPokemon.class);
        Root<CaughtPokemon> root = cq.from(CaughtPokemon.class);
        
        Predicate trainerPredicate = cb.equal(root.get("trainer").get("id"), trainerId);
        cq.where(trainerPredicate);
        cq.orderBy(cb.desc(root.get("captureDate")));
        
        return em.createQuery(cq).getResultList();
    }

    public List<CaughtPokemon> findCaughtPokemonsByPokemon(Long pokemonId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CaughtPokemon> cq = cb.createQuery(CaughtPokemon.class);
        Root<CaughtPokemon> root = cq.from(CaughtPokemon.class);
        
        Predicate pokemonPredicate = cb.equal(root.get("pokemon").get("id"), pokemonId);
        cq.where(pokemonPredicate);
        cq.orderBy(cb.desc(root.get("captureDate")));
        
        return em.createQuery(cq).getResultList();
    }

    public void deleteCaughtPokemon(Long id) {
        CaughtPokemon caughtPokemon = findCaughtPokemonById(id);
        if (caughtPokemon != null) {
            em.remove(caughtPokemon);
        }
    }
}

