package com.example.rest;

import com.example.domain.Type;
import com.example.service.TypeService;
import jakarta.ws.rs.core.Response;
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
class TypeResourceTest {

    @Mock
    private TypeService typeService;

    @InjectMocks
    private TypeResource typeResource;

    @Test
    void testCreateType() {
        // given
        Type type = new Type("Fire");
        Type created = new Type("Fire");
        created.setId(1L);

        when(typeService.createType("Fire")).thenReturn(created);

        // when
        Response response = typeResource.createType(type);

        // then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(created, response.getEntity());
        verify(typeService, times(1)).createType("Fire");
    }

    @Test
    void testGetAllTypes() {
        // given
        List<Type> types = new ArrayList<>();
        types.add(new Type("Fire"));
        types.add(new Type("Water"));
        types.add(new Type("Grass"));

        when(typeService.findAllTypes()).thenReturn(types);

        // when
        Response response = typeResource.getAllTypes();

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(types, response.getEntity());
        verify(typeService, times(1)).findAllTypes();
    }

    @Test
    void testGetTypeById() {
        // given
        Long id = 1L;
        Type type = new Type("Fire");
        type.setId(id);

        when(typeService.findTypeById(id)).thenReturn(type);

        // when
        Response response = typeResource.getTypeById(id);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(type, response.getEntity());
        verify(typeService, times(1)).findTypeById(id);
    }

    @Test
    void testGetTypeByIdNotFound() {
        // given
        Long id = 999L;

        when(typeService.findTypeById(id)).thenReturn(null);

        // when
        Response response = typeResource.getTypeById(id);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(typeService, times(1)).findTypeById(id);
    }

    @Test
    void testUpdateType() {
        // given
        Long id = 1L;
        Type type = new Type("Fire Updated");
        Type updated = new Type("Fire Updated");
        updated.setId(id);

        when(typeService.updateType(id, "Fire Updated")).thenReturn(updated);

        // when
        Response response = typeResource.updateType(id, type);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(updated, response.getEntity());
        verify(typeService, times(1)).updateType(id, "Fire Updated");
    }

    @Test
    void testUpdateTypeNotFound() {
        // given
        Long id = 999L;
        Type type = new Type("Unknown");

        when(typeService.updateType(id, "Unknown")).thenReturn(null);

        // when
        Response response = typeResource.updateType(id, type);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(typeService, times(1)).updateType(id, "Unknown");
    }

    @Test
    void testDeleteType() {
        // given
        Long id = 1L;
        doNothing().when(typeService).deleteType(id);

        // when
        Response response = typeResource.deleteType(id);

        // then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(typeService, times(1)).deleteType(id);
    }
}
