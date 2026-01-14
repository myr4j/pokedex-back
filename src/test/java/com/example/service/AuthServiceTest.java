package com.example.service;

import com.example.domain.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<Trainer> cq;

    @Mock
    private Root<Trainer> root;

    @Mock
    private Predicate predicate;

    @Mock
    private TypedQuery<Trainer> typedQuery;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterSuccess() {
        // given
        String name = "Ash";
        String email = "ash@pokemon.com";
        String password = "pikachu123";

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Trainer.class)).thenReturn(cq);
        when(cq.from(Trainer.class)).thenReturn(root);
        when(cb.equal(root.get("email"), email)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        doAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(1L);
            return null;
        }).when(em).persist(any(Trainer.class));

        // when
        Trainer result = authService.register(name, email, password);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertNotNull(result.getPassword());
        assertNotEquals(password, result.getPassword());
        assertTrue(BCrypt.checkpw(password, result.getPassword()));

        verify(em, times(1)).persist(any(Trainer.class));
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        // given
        String name = "Ash";
        String email = "ash@pokemon.com";
        String password = "pikachu123";

        Trainer existing = new Trainer(name, email, "hash");

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Trainer.class)).thenReturn(cq);
        when(cq.from(Trainer.class)).thenReturn(root);
        when(cb.equal(root.get("email"), email)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(existing);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(name, email, password));
        assertEquals("Email already exists: " + email, ex.getMessage());

        verify(em, never()).persist(any(Trainer.class));
    }

    @Test
    void testLoginSuccess() {
        // given
        String email = "ash@pokemon.com";
        String password = "pikachu123";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        Trainer trainer = new Trainer("Ash", email, hashed);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Trainer.class)).thenReturn(cq);
        when(cq.from(Trainer.class)).thenReturn(root);
        when(cb.equal(root.get("email"), email)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(trainer);

        // when
        Trainer result = authService.login(email, password);

        // then
        assertNotNull(result);
        assertEquals(trainer, result);
    }

    @Test
    void testLoginUnknownEmail() {
        // given
        String email = "unknown@pokemon.com";
        String password = "test";

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Trainer.class)).thenReturn(cq);
        when(cq.from(Trainer.class)).thenReturn(root);
        when(cb.equal(root.get("email"), email)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(email, password));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testLoginWrongPassword() {
        // given
        String email = "ash@pokemon.com";
        String password = "wrong";
        String hashed = BCrypt.hashpw("pikachu123", BCrypt.gensalt());

        Trainer trainer = new Trainer("Ash", email, hashed);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Trainer.class)).thenReturn(cq);
        when(cq.from(Trainer.class)).thenReturn(root);
        when(cb.equal(root.get("email"), email)).thenReturn(predicate);
        when(cq.where(predicate)).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(trainer);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(email, password));
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
