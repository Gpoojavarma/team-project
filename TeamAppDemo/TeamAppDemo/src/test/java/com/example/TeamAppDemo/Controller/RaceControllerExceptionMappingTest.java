package com.example.TeamAppDemo.Controller;
import com.example.TeamAppDemo.Exception.*;
import com.example.TeamAppDemo.Service.RaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RaceController.class)
@Import(ApiExceptionHandler.class)
class RaceControllerExceptionMappingTest {

    @TestConfiguration
    static class JacksonCfg {
        @Bean JavaTimeModule javaTimeModule() { return new JavaTimeModule(); }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RaceService raceService;

    @Test @DisplayName("GET /api/races/{id} -> 404 when ResourceNotFoundException")
    void getNotFound() throws Exception {
        Mockito.when(raceService.get(404L))
                .thenThrow(new ResourceNotFoundException("Race not found: 404"));

        mockMvc.perform(get("/api/races/404"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", containsString("Race not found")));
    }

    @Test @DisplayName("POST /api/races -> 400 when BusinessRuleViolationException")
    void createBusinessRuleViolation() throws Exception {
        Mockito.when(raceService.create(any()))
                .thenThrow(new BusinessRuleViolationException("Race track name must be unique"));

        String json = objectMapper.writeValueAsString(
                Map.of("trackName", "Monza",
                       "city", "Monza",
                       "country", "Italy",
                       "raceDate", LocalDate.now().plusDays(3).toString())
        );

        mockMvc.perform(post("/api/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", containsString("unique")));
    }

    @Test @DisplayName("POST /api/races -> 400 on malformed JSON")
    void createMalformedJson() throws Exception {
        String badJson = "{\"trackName\": \"Monza\", \"raceDate\": 2026-01-01 }"; // invalid JSON
        mockMvc.perform(post("/api/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", containsString("Malformed JSON")));
    }

    @Test @DisplayName("POST /api/races -> 409 when DataIntegrityViolationException")
    void createDataIntegrity409() throws Exception {
        Mockito.when(raceService.create(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        String json = objectMapper.writeValueAsString(
                Map.of("trackName", "Monza",
                       "city", "Monza",
                       "country", "Italy",
                       "raceDate", LocalDate.now().plusDays(5).toString())
        );

        mockMvc.perform(post("/api/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", containsString("already registered")));
    }

    @Test @DisplayName("PUT /api/races/{id} -> 500 on unexpected exception")
    void update500() throws Exception {
        Mockito.when(raceService.update(eq(1L), any()))
                .thenThrow(new RuntimeException("boom"));

        String json = objectMapper.writeValueAsString(
                Map.of("trackName", "X",
                       "city", "Y",
                       "country", "Z",
                       "raceDate", LocalDate.now().plusDays(10).toString())
        );

        mockMvc.perform(put("/api/races/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal error")));
    }
}
