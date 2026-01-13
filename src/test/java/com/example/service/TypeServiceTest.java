package com.example.service;

import com.example.domain.Type;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
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
class TypeServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<Type> cq;

    @Mock
    private Root<Type> root;

    @Mock
    private Path<Object> idPath;

    @Mock
    private Order order;

    @Mock
    private TypedQuery<Type> typedQuery;

    @InjectMocks
    private TypeService typeService;

    @Test
    void testCreateType() {
        // Given
        String name = "Fire";

        doAnswer(invocation -> {
            Type t = invocation.getArgument(0);
            t.setId(1L);
            return null;
        }).when(em).persist(any(Type.class));

        // When
        Type result = typeService.createType(name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(em, times(1)).persist(any(Type.class));
    }

    @Test
    void testFindTypeById() {
        // Given
        Long id = 1L;
        Type type = new Type("Water");
        type.setId(id);

        when(em.find(Type.class, id)).thenReturn(type);

        // When
        Type result = typeService.findTypeById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Water", result.getName());
        verify(em, times(1)).find(Type.class, id);
    }

    @Test
    void testFindAllTypes() {
        // Given
        List<Type> types = new ArrayList<>();
        types.add(new Type("Grass"));
        types.add(new Type("Electric"));

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Type.class)).thenReturn(cq);
        when(cq.from(Type.class)).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(root.get(anyString())).thenReturn(idPath);
        when(cb.asc(any())).thenReturn(order);
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(types);

        // When
        List<Type> result = typeService.findAllTypes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(em, times(1)).getCriteriaBuilder();
        verify(cb, times(1)).createQuery(Type.class);
        verify(cq, times(1)).from(Type.class);
        verify(cq, times(1)).select(root);
        verify(cb, times(1)).asc(any());
        verify(cq, times(1)).orderBy(any(Order.class));
        verify(em, times(1)).createQuery(cq);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void testUpdateType() {
        // Given
        Long id = 1L;
        Type existingType = new Type("Psychic");
        existingType.setId(id);

        when(em.find(Type.class, id)).thenReturn(existingType);
        when(em.merge(any(Type.class))).thenReturn(existingType);

        // When
        Type result = typeService.updateType(id, "Dark");

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Dark", result.getName());
        verify(em, times(1)).find(Type.class, id);
        verify(em, times(1)).merge(existingType);
    }

    @Test
    void testUpdateTypeNotFound() {
        // Given
        Long id = 999L;
        when(em.find(Type.class, id)).thenReturn(null);

        // When
        Type result = typeService.updateType(id, "Flying");

        // Then
        assertNull(result);
        verify(em, times(1)).find(Type.class, id);
        verify(em, never()).merge(any(Type.class));
    }

    @Test
    void testDeleteType() {
        // Given
        Long id = 1L;
        Type type = new Type("Flying");
        type.setId(id);

        when(em.find(Type.class, id)).thenReturn(type);
        doNothing().when(em).remove(any(Type.class));

        // When
        typeService.deleteType(id);

        // Then
        verify(em, times(1)).find(Type.class, id);
        verify(em, times(1)).remove(type);
    }

    @Test
    void testDeleteTypeNotFound() {
        // Given
        Long id = 999L;
        when(em.find(Type.class, id)).thenReturn(null);

        // When
        typeService.deleteType(id);

        // Then
        verify(em, times(1)).find(Type.class, id);
        verify(em, never()).remove(any(Type.class));
    }
}
