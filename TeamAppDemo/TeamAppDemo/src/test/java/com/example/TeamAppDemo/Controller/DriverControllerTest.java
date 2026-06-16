package com.example.TeamAppDemo.Controller;

import com.example.TeamAppDemo.DTO.DriverDtos;
import com.example.TeamAppDemo.Entity.Driver;
import com.example.TeamAppDemo.Entity.DriverRace;
import com.example.TeamAppDemo.Entity.Race;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Service.DriverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest slice for DriverController.
 * Mocks DriverService and DriverRaceRepository (used inside toResponse()).
 */
@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @TestConfiguration
    static class JacksonConfigForTests {
        @Bean
        JavaTimeModule javaTimeModule() {
            // Support LocalDate in request/response bodies
            return new JavaTimeModule();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DriverService driverService;
    @MockBean private DriverRaceRepository driverRaceRepository;

    // ---------- Helpers ----------

    // Replace the previous makeDriver* helpers with these:

private Driver makeDriver(long id) {
    Driver d = new Driver();
    d.setId(id);
    d.setFirstName("Ayrton");
    d.setLastName("Senna");
    d.setDateOfBirth(LocalDate.of(1960, 3, 21));
    // Do NOT touch driverRaces here (no setter)
    return d;
}


private Driver makeDriverWithTeamAndRaces(long id, Long teamId, List<Long> raceIds) {
    Driver base = makeDriver(id);
    Driver d = Mockito.spy(base);

    if (teamId != null) {
        Team t = new Team();
        t.setId(teamId);
        t.setName("McLaren");
        Mockito.when(d.getTeam()).thenReturn(t);
    }

    // Use a Set instead of List
    Set<DriverRace> drs = new HashSet<>();
    for (Long rId : raceIds) {

        
Race r = new Race();
        r.setId(rId);

        DriverRace dr = new DriverRace();
        dr.setRace(r);
        dr.setDriver(d);

        drs.add(dr);
    }

    // Stub the getter with Set<DriverRace>
    Mockito.when(d.getDriverRaces()).thenReturn(drs);

    return d;
}

    // ---------- Tests ----------

    @Test
    @DisplayName("POST /api/drivers : create driver - 200 OK when payload valid")
    void createDriver() throws Exception {
        Driver created = makeDriver(1L);

        Mockito.when(driverService.create(ArgumentMatchers.any(DriverDtos.DriverCreateRequest.class)))
               .thenReturn(created);
        // registrationCount is computed by controller; return a deterministic value
        Mockito.when(driverRaceRepository.countByDriver_Id(1L)).thenReturn(0L);

        Map<String, Object> req = Map.of(
                "firstName", "Ayrton",
                "lastName", "Senna",
                "dateOfBirth", "1960-03-21"
        );

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.firstName", is("Ayrton")))
               .andExpect(jsonPath("$.lastName", is("Senna")))
               .andExpect(jsonPath("$.teamId").doesNotExist()) // null omitted by Jackson
               .andExpect(jsonPath("$.raceIds", hasSize(0)))
               .andExpect(jsonPath("$.registrationCount", is(0)));
    }

    @Test
    @DisplayName("PUT /api/drivers/{id} : update driver - 200 OK when payload valid")
    void updateDriver() throws Exception {
        Driver updated = makeDriver(2L);
        updated.setFirstName("Ayrton Updated");

        Mockito.when(driverService.update(Mockito.eq(2L),
                        ArgumentMatchers.any(DriverDtos.DriverUpdateRequest.class)))
               .thenReturn(updated);
        Mockito.when(driverRaceRepository.countByDriver_Id(2L)).thenReturn(3L);

        Map<String, Object> req = Map.of(
                "firstName", "Ayrton Updated",
                "lastName", "Senna",
                "dateOfBirth", "1960-03-21"
        );

        mockMvc.perform(put("/api/drivers/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(2)))
               .andExpect(jsonPath("$.firstName", is("Ayrton Updated")))
               .andExpect(jsonPath("$.registrationCount", is(3)));
    }

    @Test
    @DisplayName("GET /api/drivers/{id} : get driver")
    void getDriver() throws Exception {
        Driver d = makeDriverWithTeamAndRaces(3L, 33L, List.of(101L, 102L));
        Mockito.when(driverService.get(3L)).thenReturn(d);
        Mockito.when(driverRaceRepository.countByDriver_Id(3L)).thenReturn(2L);

        mockMvc.perform(get("/api/drivers/3").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(3)))
               .andExpect(jsonPath("$.firstName", is("Ayrton")))
               .andExpect(jsonPath("$.teamId", is(33)))
               .andExpect(jsonPath("$.raceIds", containsInAnyOrder(101, 102)))
               .andExpect(jsonPath("$.registrationCount", is(2)));
    }

    @Test
    @DisplayName("GET /api/drivers : list drivers")
    void listDrivers() throws Exception {
        Driver d1 = makeDriver(4L);
        Driver d2 = makeDriverWithTeamAndRaces(5L, 55L, List.of(201L));
        Mockito.when(driverService.list()).thenReturn(List.of(d1, d2));
        Mockito.when(driverRaceRepository.countByDriver_Id(4L)).thenReturn(0L);
        Mockito.when(driverRaceRepository.countByDriver_Id(5L)).thenReturn(1L);

        mockMvc.perform(get("/api/drivers").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].id", is(4)))
               .andExpect(jsonPath("$[0].registrationCount", is(0)))
               .andExpect(jsonPath("$[1].id", is(5)))
               .andExpect(jsonPath("$[1].teamId", is(55)))
               .andExpect(jsonPath("$[1].raceIds", contains(201)))
               .andExpect(jsonPath("$[1].registrationCount", is(1)));
    }

    @Test
    @DisplayName("DELETE /api/drivers/{id} : delete driver")
    void deleteDriver() throws Exception {
        mockMvc.perform(delete("/api/drivers/9"))
               .andExpect(status().isOk());
        Mockito.verify(driverService).delete(9L);
    }

    @Test
    @DisplayName("PUT /api/drivers/{id}/team/{teamId} : assign team to driver")
    void assignTeam() throws Exception {
        Driver afterAssign = makeDriverWithTeamAndRaces(7L, 77L, List.of());
        Mockito.when(driverService.assignTeam(7L, 77L)).thenReturn(afterAssign);
        Mockito.when(driverRaceRepository.countByDriver_Id(7L)).thenReturn(0L);

        mockMvc.perform(put("/api/drivers/7/team/77").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(7)))
               .andExpect(jsonPath("$.teamId", is(77)))
               .andExpect(jsonPath("$.registrationCount", is(0)));
    }
}