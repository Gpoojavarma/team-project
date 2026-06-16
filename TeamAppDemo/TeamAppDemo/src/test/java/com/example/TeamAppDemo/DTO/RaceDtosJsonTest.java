package com.example.TeamAppDemo.DTO;
import com.example.TeamAppDemo.DTO.RaceDtos.RaceCreateRequest;
import com.example.TeamAppDemo.DTO.RaceDtos.RaceUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RaceDtosJsonTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test @DisplayName("RaceCreateRequest JSON roundtrip with null closure")
    void createRoundtrip() throws Exception {
        RaceCreateRequest req = new RaceCreateRequest();
        req.trackName = "Monza";
        req.city = "Monza";
        req.country = "Italy";
        req.raceDate = LocalDate.now().plusDays(5);
        req.registrationClosureDate = null;

        String json = mapper.writeValueAsString(req);
        RaceCreateRequest back = mapper.readValue(json, RaceCreateRequest.class);

        assertThat(back.trackName).isEqualTo("Monza");
        assertThat(back.registrationClosureDate).isNull();
    }

    @Test @DisplayName("RaceUpdateRequest JSON roundtrip with closure today")
    void updateRoundtrip() throws Exception {
        RaceUpdateRequest req = new RaceUpdateRequest();
        req.trackName = "Spa";
        req.city = "Stavelot";
        req.country = "Belgium";
        req.raceDate = LocalDate.now().plusDays(10);
        req.registrationClosureDate = LocalDate.now();

        String json = mapper.writeValueAsString(req);
        RaceUpdateRequest back = mapper.readValue(json, RaceUpdateRequest.class);

        assertThat(back.raceDate).isEqualTo(req.raceDate);
        assertThat(back.registrationClosureDate).isEqualTo(req.registrationClosureDate);
    }
}
