package com.example.rest;

import com.example.domain.Trainer;
import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.messaging.TrainerMessageProducer;
import com.example.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    private AuthService authService;

    @Mock
    private TrainerMessageProducer trainerMessageProducer;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private AuthResource authResource;

    @Test
    void testRegisterSuccess() {
        // given
        RegisterRequest request = new RegisterRequest("Ash", "ash@pokemon.com", "pikachu123");
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com", "hashed");
        trainer.setId(1L);

        when(authService.register("Ash", "ash@pokemon.com", "pikachu123")).thenReturn(trainer);
        doNothing().when(trainerMessageProducer).sendTrainerCreatedMessage(any());

        // when
        Response response = authResource.register(request);

        // then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof AuthResponse);
        AuthResponse body = (AuthResponse) response.getEntity();
        assertEquals(1L, body.getTrainerId());
        assertEquals("ash@pokemon.com", body.getEmail());
        assertEquals("Ash", body.getName());

        verify(authService, times(1)).register("Ash", "ash@pokemon.com", "pikachu123");
        verify(trainerMessageProducer, times(1)).sendTrainerCreatedMessage(any());
    }

    @Test
    void testRegisterWithNullRequest() {
        // when
        Response response = authResource.register(null);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("name, email and password are required", response.getEntity());
        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void testRegisterWithMissingName() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("ash@pokemon.com");
        request.setPassword("pikachu123");

        // when
        Response response = authResource.register(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void testRegisterWithMissingEmail() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setName("Ash");
        request.setPassword("pikachu123");

        // when
        Response response = authResource.register(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void testRegisterWithMissingPassword() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setName("Ash");
        request.setEmail("ash@pokemon.com");

        // when
        Response response = authResource.register(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        // given
        RegisterRequest request = new RegisterRequest("Ash", "ash@pokemon.com", "pikachu123");

        when(authService.register("Ash", "ash@pokemon.com", "pikachu123"))
                .thenThrow(new IllegalArgumentException("Email already exists: ash@pokemon.com"));

        // when
        Response response = authResource.register(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Email already exists: ash@pokemon.com", response.getEntity());
    }

    @Test
    void testLoginSuccess() {
        // given
        LoginRequest request = new LoginRequest("ash@pokemon.com", "pikachu123");
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com", "hashed");
        trainer.setId(1L);

        when(authService.login("ash@pokemon.com", "pikachu123")).thenReturn(trainer);
        when(httpRequest.getSession(true)).thenReturn(httpSession);

        // when
        Response response = authResource.login(request);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof AuthResponse);
        AuthResponse body = (AuthResponse) response.getEntity();
        assertEquals(1L, body.getTrainerId());
        assertEquals("ash@pokemon.com", body.getEmail());
        assertEquals("Ash", body.getName());

        verify(authService, times(1)).login("ash@pokemon.com", "pikachu123");
        verify(httpRequest, times(1)).getSession(true);
        verify(httpSession, times(1)).setAttribute("trainerId", 1L);
    }

    @Test
    void testLoginWithNullRequest() {
        // when
        Response response = authResource.login(null);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("email and password are required", response.getEntity());
        verify(authService, never()).login(any(), any());
    }

    @Test
    void testLoginWithMissingEmail() {
        // given
        LoginRequest request = new LoginRequest();
        request.setPassword("pikachu123");

        // when
        Response response = authResource.login(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(authService, never()).login(any(), any());
    }

    @Test
    void testLoginWithMissingPassword() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("ash@pokemon.com");

        // when
        Response response = authResource.login(request);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(authService, never()).login(any(), any());
    }

    @Test
    void testLoginInvalidCredentials() {
        // given
        LoginRequest request = new LoginRequest("ash@pokemon.com", "wrong");

        when(authService.login("ash@pokemon.com", "wrong"))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));

        // when
        Response response = authResource.login(request);

        // then
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Invalid email or password", response.getEntity());
    }

    @Test
    void testLogoutWithSession() {
        // given
        when(httpRequest.getSession(false)).thenReturn(httpSession);
        when(httpRequest.getSession()).thenReturn(httpSession);

        // when
        Response response = authResource.logout();

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("logged out successfully", response.getEntity());
        verify(httpRequest, times(1)).getSession(false);
        verify(httpRequest, times(1)).getSession();
        verify(httpSession, times(1)).invalidate();
    }

    @Test
    void testLogoutWithoutSession() {
        // given
        when(httpRequest.getSession(false)).thenReturn(null);

        // when
        Response response = authResource.logout();

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("logged out successfully", response.getEntity());
        verify(httpRequest, times(1)).getSession(false);
        verify(httpSession, never()).invalidate();
    }
}
