package com.example.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private ResourceInfo resourceInfo;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private HttpSession session;

    private AuthFilter authFilter;

    // classe de test pour simuler une ressource @Secured
    @Secured
    static class SecuredResource {
        public void securedMethod() {}
    }

    // classe de test pour simuler une ressource publique
    static class PublicResource {
        public void publicMethod() {}
    }

    @BeforeEach
    void setUp() throws Exception {
        authFilter = new AuthFilter();
        
        // injecter les mocks dans le filtre via reflection
        Field resourceInfoField = AuthFilter.class.getDeclaredField("resourceInfo");
        resourceInfoField.setAccessible(true);
        resourceInfoField.set(authFilter, resourceInfo);

        Field requestField = AuthFilter.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(authFilter, request);
    }

    @Test
    void testFilterWithPublicResource() {
        // given
        when(resourceInfo.getResourceClass()).thenReturn((Class) PublicResource.class);
        when(resourceInfo.getResourceMethod()).thenReturn(getMethod(PublicResource.class, "publicMethod"));

        // when
        authFilter.filter(requestContext);

        // then
        verify(requestContext, never()).abortWith(any());
        verify(request, never()).getSession(anyBoolean());
    }

    @Test
    void testFilterWithSecuredResourceAndValidSession() {
        // given
        when(resourceInfo.getResourceClass()).thenReturn((Class) SecuredResource.class);
        when(resourceInfo.getResourceMethod()).thenReturn(getMethod(SecuredResource.class, "securedMethod"));
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("trainerId")).thenReturn(1L);

        // when
        authFilter.filter(requestContext);

        // then
        verify(requestContext, never()).abortWith(any());
        verify(request, times(1)).getSession(false);
        verify(session, times(1)).getAttribute("trainerId");
    }

    @Test
    void testFilterWithSecuredResourceAndNoSession() {
        // given
        when(resourceInfo.getResourceClass()).thenReturn((Class) SecuredResource.class);
        when(resourceInfo.getResourceMethod()).thenReturn(getMethod(SecuredResource.class, "securedMethod"));
        when(request.getSession(false)).thenReturn(null);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // when
        authFilter.filter(requestContext);

        // then
        verify(requestContext, times(1)).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), capturedResponse.getStatus());
        assertEquals("authentication required", capturedResponse.getEntity());
        verify(request, times(1)).getSession(false);
    }

    @Test
    void testFilterWithSecuredResourceAndSessionWithoutTrainerId() {
        // given
        when(resourceInfo.getResourceClass()).thenReturn((Class) SecuredResource.class);
        when(resourceInfo.getResourceMethod()).thenReturn(getMethod(SecuredResource.class, "securedMethod"));
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("trainerId")).thenReturn(null);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // when
        authFilter.filter(requestContext);

        // then
        verify(requestContext, times(1)).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), capturedResponse.getStatus());
        assertEquals("authentication required", capturedResponse.getEntity());
        verify(request, times(1)).getSession(false);
        verify(session, times(1)).getAttribute("trainerId");
    }

    @Test
    void testFilterWithSecuredMethod() throws NoSuchMethodException {
        // given - méthode annotée @Secured dans une classe publique
        when(resourceInfo.getResourceClass()).thenReturn((Class) SecuredResource.class);
        Method securedMethod = SecuredResource.class.getMethod("securedMethod");
        when(resourceInfo.getResourceMethod()).thenReturn(securedMethod);
        when(request.getSession(false)).thenReturn(null);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // when
        authFilter.filter(requestContext);

        // then - la classe est @Secured donc ça doit bloquer
        verify(requestContext, times(1)).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), capturedResponse.getStatus());
        assertEquals("authentication required", capturedResponse.getEntity());
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
