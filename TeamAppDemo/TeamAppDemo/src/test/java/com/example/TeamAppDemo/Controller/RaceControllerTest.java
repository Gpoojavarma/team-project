
package com.example.TeamAppDemo.Controller;

import com.example.TeamAppDemo.DTO.RaceDtos;
import com.example.TeamAppDemo.Entity.Race;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RaceController.class)
class RaceControllerTest {

    @TestConfiguration
    static class JacksonConfigForTests {
        @Bean
        JavaTimeModule javaTimeModule() {
            // Ensure LocalDate is fully supported inside the MVC test slice
            return new JavaTimeModule();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RaceService raceService;

    private Race makeRace(long id) {
        Race r = new Race();
        r.setId(id);
        r.setTrackName("Monza");
        r.setCity("Monza");
        r.setCountry("Italy");
        r.setRaceDate(LocalDate.now().plusDays(10));
        r.setRegistrationClosureDate(LocalDate.now()); // response can include today; fine for @PastOrPresent
        return r;
    }

    @Test
    @DisplayName("POST /api/races : create race - 200 OK when payload valid")
    void createRace() throws Exception {
        Race created = makeRace(1L);
        Mockito.when(raceService.create(Mockito.any(RaceDtos.RaceCreateRequest.class))).thenReturn(created);

        String raceDateStr = LocalDate.now().plusDays(20).toString();

        // Use a mutable map and OMIT 'registrationClosureDate' entirely (null is allowed and passes @PastOrPresent)
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("trackName", "Monza");
        reqBody.put("city", "Monza");
        reqBody.put("country", "Italy");
        reqBody.put("raceDate", raceDateStr);

        mockMvc.perform(post("/api/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.trackName", is("Monza")));
    }

    @Test
    @DisplayName("PUT /api/races/{id} : update race - 200 OK when payload valid")
    void updateRace() throws Exception {
        Race updated = makeRace(2L);
        updated.setTrackName("Updated");
        Mockito.when(raceService.update(Mockito.eq(2L), Mockito.any(RaceDtos.RaceUpdateRequest.class))).thenReturn(updated);

        String raceDateStr = LocalDate.now().plusDays(30).toString();

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("trackName", "Updated");
        reqBody.put("city", "Monza");
        reqBody.put("country", "Italy");
        reqBody.put("raceDate", raceDateStr);
        // Omit registrationClosureDate (optional and @PastOrPresent when present)

        mockMvc.perform(put("/api/races/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.trackName", is("Updated")));
    }

    @Test
    @DisplayName("GET /api/races/{id} : get race")
    void getRace() throws Exception {
        Race r = makeRace(3L);
        Mockito.when(raceService.get(3L)).thenReturn(r);

        mockMvc.perform(get("/api/races/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.trackName", is("Monza")));
    }

    @Test
    @DisplayName("GET /api/races : list races")
    void listRaces() throws Exception {
        Race r1 = makeRace(4L);
        Race r2 = makeRace(5L);
        Mockito.when(raceService.list()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/races").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(4)))
                .andExpect(jsonPath("$[1].id", is(5)));
    }

    @Test
    @DisplayName("DELETE /api/races/{id} : delete race")
    void deleteRace() throws Exception {
        mockMvc.perform(delete("/api/races/9"))
                .andExpect(status().isOk());
        Mockito.verify(raceService).delete(9L);
    }

    @Test
    @DisplayName("POST /api/races/{raceId}/registrations/{driverId} : register driver")
    void registerDriver() throws Exception {
        mockMvc.perform(post("/api/races/10/registrations/20"))
                .andExpect(status().isOk());
        Mockito.verify(raceService).registerDriver(10L, 20L);
    }

    @Test
    @DisplayName("DELETE /api/races/{raceId}/registrations/{driverId} : unregister driver")
    void unregisterDriver() throws Exception {
        mockMvc.perform(delete("/api/races/10/registrations/20"))
                .andExpect(status().isOk());
        Mockito.verify(raceService).unregisterDriver(10L, 20L);
    }
}
