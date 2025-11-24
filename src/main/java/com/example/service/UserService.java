package com.example.service;

import com.example.domain.User;
import com.example.messaging.UserCreatedProducer;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserService {

    @PersistenceContext
    EntityManager em;

    @Inject
    UserCreatedProducer producer;

    public User createUser(String name, String email) {
        User u = new User(name, email);
        em.persist(u);
        producer.sendUserCreatedEvent(u);
        return u;
    }

    public User findUser(Long id) {
        return em.find(User.class, id);
    }
}
