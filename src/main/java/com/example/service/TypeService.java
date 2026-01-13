package com.example.service;

import com.example.domain.Type;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Stateless
public class TypeService {

    @PersistenceContext
    private EntityManager em;

    public Type createType(String name) {
        Type type = new Type(name);
        em.persist(type);
        return type;
    }

    public Type findTypeById(Long id) {
        return em.find(Type.class, id);
    }

    public List<Type> findAllTypes() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Type> cq = cb.createQuery(Type.class);
        Root<Type> root = cq.from(Type.class);
        cq.select(root);
        cq.orderBy(cb.asc(root.get("name")));
        return em.createQuery(cq).getResultList();
    }

    public Type updateType(Long id, String name) {
        Type type = findTypeById(id);
        if (type != null) {
            type.setName(name);
            em.merge(type);
        }
        return type;
    }

    public void deleteType(Long id) {
        Type type = findTypeById(id);
        if (type != null) {
            em.remove(type);
        }
    }
}
