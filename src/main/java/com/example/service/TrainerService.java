package com.example.service;

import com.example.domain.Trainer;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TrainerService {

    @PersistenceContext
    private EntityManager em;

    public Trainer createTrainer(String name, String email) {
        Trainer trainer = new Trainer(name, email);
        em.persist(trainer);
        return trainer;
    }

    public Trainer findTrainerById(Long id) {
        return em.find(Trainer.class, id);
    }

    public List<Trainer> findAllTrainers() {
        return em.createQuery("SELECT t FROM Trainer t", Trainer.class)
                .getResultList();
    }

    public Trainer updateTrainer(Long id, String name, String email) {
        Trainer trainer = findTrainerById(id);
        if (trainer != null) {
            trainer.setName(name);
            trainer.setEmail(email);
            em.merge(trainer);
        }
        return trainer;
    }

    public void deleteTrainer(Long id) {
        Trainer trainer = findTrainerById(id);
        if (trainer != null) {
            em.remove(trainer);
        }
    }
}
