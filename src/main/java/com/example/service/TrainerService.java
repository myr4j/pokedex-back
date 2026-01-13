package com.example.service;

import com.example.domain.Trainer;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Trainer> cq = cb.createQuery(Trainer.class);
        Root<Trainer> root = cq.from(Trainer.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
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
