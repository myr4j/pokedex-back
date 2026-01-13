package com.example.rest;

import com.example.domain.Trainer;
import com.example.dto.TrainerStats;
import com.example.service.TrainerService;
import com.example.service.TrainerStatsService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerResourceTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainerStatsService trainerStatsService;

    @InjectMocks
    private TrainerResource trainerResource;

    @Test
    void testCreateTrainer() {
        // given
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        Trainer created = new Trainer("Ash", "ash@pokemon.com");
        created.setId(1L);

        when(trainerService.createTrainer("Ash", "ash@pokemon.com")).thenReturn(created);

        // when
        Response response = trainerResource.createTrainer(trainer);

        // then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(created, response.getEntity());
        verify(trainerService, times(1)).createTrainer("Ash", "ash@pokemon.com");
    }

    @Test
    void testGetAllTrainers() {
        // given
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(new Trainer("Ash", "ash@pokemon.com"));
        trainers.add(new Trainer("Misty", "misty@pokemon.com"));

        when(trainerService.findAllTrainers()).thenReturn(trainers);

        // when
        Response response = trainerResource.getAllTrainers();

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(trainers, response.getEntity());
        verify(trainerService, times(1)).findAllTrainers();
    }

    @Test
    void testGetTrainerById() {
        // given
        Long id = 1L;
        Trainer trainer = new Trainer("Ash", "ash@pokemon.com");
        trainer.setId(id);

        when(trainerService.findTrainerById(id)).thenReturn(trainer);

        // when
        Response response = trainerResource.getTrainerById(id);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(trainer, response.getEntity());
        verify(trainerService, times(1)).findTrainerById(id);
    }

    @Test
    void testGetTrainerByIdNotFound() {
        // given
        Long id = 999L;

        when(trainerService.findTrainerById(id)).thenReturn(null);

        // when
        Response response = trainerResource.getTrainerById(id);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(trainerService, times(1)).findTrainerById(id);
    }

    @Test
    void testUpdateTrainer() {
        // given
        Long id = 1L;
        Trainer trainer = new Trainer("Ash Updated", "ash.updated@pokemon.com");
        Trainer updated = new Trainer("Ash Updated", "ash.updated@pokemon.com");
        updated.setId(id);

        when(trainerService.updateTrainer(id, "Ash Updated", "ash.updated@pokemon.com"))
                .thenReturn(updated);

        // when
        Response response = trainerResource.updateTrainer(id, trainer);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(updated, response.getEntity());
        verify(trainerService, times(1))
                .updateTrainer(id, "Ash Updated", "ash.updated@pokemon.com");
    }

    @Test
    void testUpdateTrainerNotFound() {
        // given
        Long id = 999L;
        Trainer trainer = new Trainer("Unknown", "unknown@pokemon.com");

        when(trainerService.updateTrainer(id, "Unknown", "unknown@pokemon.com"))
                .thenReturn(null);

        // when
        Response response = trainerResource.updateTrainer(id, trainer);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(trainerService, times(1))
                .updateTrainer(id, "Unknown", "unknown@pokemon.com");
    }

    @Test
    void testDeleteTrainer() {
        // given
        Long id = 1L;
        doNothing().when(trainerService).deleteTrainer(id);

        // when
        Response response = trainerResource.deleteTrainer(id);

        // then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(trainerService, times(1)).deleteTrainer(id);
    }

    @Test
    void testGetTrainerStats() {
        // given
        Long id = 1L;
        TrainerStats stats = new TrainerStats();
        stats.setTrainerId(id);
        stats.setTrainerName("Ash");
        stats.setTotalCaptures(10L);
        stats.setUniquePokemons(8L);
        stats.setPokedexCompletionPercentage(8.0);
        stats.setCapturesByType(new HashMap<>());
        stats.setTotalPokemonsInPokedex(100L);

        when(trainerStatsService.getTrainerStats(id)).thenReturn(stats);

        // when
        Response response = trainerResource.getTrainerStats(id);

        // then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(stats, response.getEntity());
        verify(trainerStatsService, times(1)).getTrainerStats(id);
    }

    @Test
    void testGetTrainerStatsNotFound() {
        // given
        Long id = 999L;

        when(trainerStatsService.getTrainerStats(id))
                .thenThrow(new IllegalArgumentException("Trainer not found with id: " + id));

        // when
        Response response = trainerResource.getTrainerStats(id);

        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Trainer not found with id: " + id, response.getEntity());
        verify(trainerStatsService, times(1)).getTrainerStats(id);
    }
}
