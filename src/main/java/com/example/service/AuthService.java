package com.example.service;

import com.example.domain.Trainer;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mindrot.jbcrypt.BCrypt;

@Stateless
public class AuthService {

    @PersistenceContext
    private EntityManager em;

    public Trainer register(String name, String email, String password) {
        // vérifier si l'email existe déjà
        Trainer existing = findTrainerByEmail(email);
        if (existing != null) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        // hasher le mot de passe
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // créer le trainer avec le mot de passe hashé
        Trainer trainer = new Trainer(name, email, hashedPassword);
        em.persist(trainer);
        return trainer;
    }

    public Trainer login(String email, String password) {
        // trouver le trainer par email
        Trainer trainer = findTrainerByEmail(email);
        if (trainer == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // vérifier le mot de passe
        if (!BCrypt.checkpw(password, trainer.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return trainer;
    }

    private Trainer findTrainerByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Trainer> cq = cb.createQuery(Trainer.class);
        Root<Trainer> root = cq.from(Trainer.class);
        
        Predicate emailPredicate = cb.equal(root.get("email"), email);
        cq.where(emailPredicate);
        
        try {
            return em.createQuery(cq).getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }
}
