package com.example.TeamAppDemo.Controller;

import com.example.TeamAppDemo.Exception.ApiExceptionHandler;
import com.example.TeamAppDemo.Service.RaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RaceController.class)
@Import(ApiExceptionHandler.class)
class RaceControllerValidationTest {

    @TestConfiguration
    static class JacksonCfg {
        @Bean JavaTimeModule javaTimeModule() { return new JavaTimeModule(); }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RaceService raceService; // not invoked when validation fails (400)

    private String toJson(Map<String, Object> m) throws Exception {
        return objectMapper.writeValueAsString(m);
    }

    @Nested
    @DisplayName("Create – field validations")
    class CreateValidations {

        @Test @DisplayName("trackName @NotBlank -> 400")
        void trackNameBlank() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("trackName", " ");
            body.put("city", "Monza");
            body.put("country", "Italy");
            body.put("raceDate", LocalDate.now().plusDays(5).toString());

            mockMvc.perform(post("/api/races")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation failed")))
                    .andExpect(jsonPath("$.details[*]", hasItem(containsString("trackName"))));
        }

        @Test @DisplayName("city @Size(max=128) -> 400")
        void cityTooLong() throws Exception {
            String longCity = "C".repeat(129);
            Map<String, Object> body = new HashMap<>();
            body.put("trackName", "Monza");
            body.put("city", longCity);
            body.put("country", "Italy");
            body.put("raceDate", LocalDate.now().plusDays(5).toString());

            mockMvc.perform(post("/api/races")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[*]", hasItem(containsString("city"))));
        }

        @Test @DisplayName("country @NotBlank -> 400")
        void countryBlank() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("trackName", "Monza");
            body.put("city", "Monza");
            body.put("country", "");
            body.put("raceDate", LocalDate.now().plusDays(5).toString());

            mockMvc.perform(post("/api/races")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[*]", hasItem(containsString("country"))));
        }

        @Test @DisplayName("raceDate @Future violated (today) -> 400")
        void raceDateNotFuture() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("trackName", "Monza");
            body.put("city", "Monza");
            body.put("country", "Italy");
            body.put("raceDate", LocalDate.now().toString()); // violates @Future

            mockMvc.perform(post("/api/races")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[*]", hasItem(containsString("raceDate"))));
        }

        @Test @DisplayName("registrationClosureDate @PastOrPresent violated -> 400")
        void closureFuture() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("trackName", "Monza");
            body.put("city", "Monza");
            body.put("country", "Italy");
            body.put("raceDate", LocalDate.now().plusDays(1).toString());
            body.put("registrationClosureDate", LocalDate.now().plusDays(5).toString());

            mockMvc.perform(post("/api/races")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details[*]", hasItem(containsString("registrationClosureDate"))));
        }
    }
}

