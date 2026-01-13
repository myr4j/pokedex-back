package com.example.service;

import com.example.domain.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<Trainer> cq;

    @Mock
    private Root<Trainer> root;

    @Mock
    private TypedQuery<Trainer> typedQuery;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void testCreateTrainer() {
        // Given
        String name = "myr";
        String email = "th@pokemon.com";

        doAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(1L);
            return null;
        }).when(em).persist(any(Trainer.class));

        // When
        Trainer result = trainerService.createTrainer(name, email);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        verify(em, times(1)).persist(any(Trainer.class));
    }

    @Test
    void testFindTrainerById() {
        // Given
        Long id = 1L;
        Trainer trainer = new Trainer("Raph", "raph@pokemon.com");
        trainer.setId(id);

        when(em.find(Trainer.class, id)).thenReturn(trainer);

        // When
        Trainer result = trainerService.findTrainerById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Raph", result.getName());
        verify(em, times(1)).find(Trainer.class, id);
    }

    @Test
    void testFindTrainerByIdNotFound() {
        // Given
        Long id = 999L;
        when(em.find(Trainer.class, id)).thenReturn(null);

        // When
        Trainer result = trainerService.findTrainerById(id);

        // Then
        assertNull(result);
        verify(em, times(1)).find(Trainer.class, id);
    }

    @Test
    void testFindAllTrainers() {
        // Given
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(new Trainer("Yanis", "yanis@pokemon.com"));
        trainers.add(new Trainer("Rahim", "rahim@pokemon.com"));

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Trainer.class)).thenReturn(cq);
        when(cq.from(Trainer.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(trainers);

        // When
        List<Trainer> result = trainerService.findAllTrainers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Trainer.class);
        verify(cq, times(1)).from(Trainer.class);
        verify(cq, times(1)).select(root);
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testUpdateTrainer() {
        // Given
        Long id = 1L;
        Trainer existingTrainer = new Trainer("Original Name", "original@pokemon.com");
        existingTrainer.setId(id);

        when(em.find(Trainer.class, id)).thenReturn(existingTrainer);
        when(em.merge(any(Trainer.class))).thenReturn(existingTrainer);

        // When
        Trainer result = trainerService.updateTrainer(id, "Updated Name", "updated@pokemon.com");

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@pokemon.com", result.getEmail());
        verify(em, times(1)).find(Trainer.class, id);
        verify(em, times(1)).merge(existingTrainer);
    }

    @Test
    void testUpdateTrainerNotFound() {
        // Given
        Long id = 999L;
        when(em.find(Trainer.class, id)).thenReturn(null);

        // When
        Trainer result = trainerService.updateTrainer(id, "Name", "email@pokemon.com");

        // Then
        assertNull(result);
        verify(em, times(1)).find(Trainer.class, id);
        verify(em, never()).merge(any(Trainer.class));
    }

    @Test
    void testDeleteTrainer() {
        // Given
        Long id = 1L;
        Trainer trainer = new Trainer("To Delete", "delete@pokemon.com");
        trainer.setId(id);

        when(em.find(Trainer.class, id)).thenReturn(trainer);
        doNothing().when(em).remove(any(Trainer.class));

        // When
        trainerService.deleteTrainer(id);

        // Then
        verify(em, times(1)).find(Trainer.class, id);
        verify(em, times(1)).remove(trainer);
    }

    @Test
    void testDeleteTrainerNotFound() {
        // Given
        Long id = 999L;
        when(em.find(Trainer.class, id)).thenReturn(null);

        // When
        trainerService.deleteTrainer(id);

        // Then
        verify(em, times(1)).find(Trainer.class, id);
        verify(em, never()).remove(any(Trainer.class));
    }
}
