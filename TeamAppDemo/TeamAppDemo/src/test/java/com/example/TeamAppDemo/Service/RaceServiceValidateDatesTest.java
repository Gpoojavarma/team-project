
package com.example.TeamAppDemo.Service;

import com.example.TeamAppDemo.DTO.RaceDtos;
import com.example.TeamAppDemo.Entity.Race;
import com.example.TeamAppDemo.Exception.BusinessRuleViolationException;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Repository.DriverRepository;
import com.example.TeamAppDemo.Repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaceServiceValidateDatesTest {

    @Mock private RaceRepository raceRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private DriverRaceRepository driverRaceRepository;

    @InjectMocks private RaceService raceService;

    private RaceDtos.RaceCreateRequest createReq;
    private RaceDtos.RaceUpdateRequest updateReq;

    @BeforeEach
    void init() {
        createReq = new RaceDtos.RaceCreateRequest();
        createReq.trackName = "Spa";
        createReq.city = "Stavelot";
        createReq.country = "Belgium";

        updateReq = new RaceDtos.RaceUpdateRequest();
        updateReq.trackName = "Spa";
        updateReq.city = "Stavelot";
        updateReq.country = "Belgium";
    }

    @Test
    @DisplayName("create(): throws when raceDate is null")
    void create_nullRaceDate() {
        createReq.raceDate = null;
        assertThatThrownBy(() -> raceService.create(createReq))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Race date is mandatory");
    }

    @Test
    @DisplayName("create(): throws when raceDate not future")
    void create_raceDateNotFuture() {
        createReq.raceDate = LocalDate.now(); // today, violates "strictly in the future"
        assertThatThrownBy(() -> raceService.create(createReq))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("in the future");
    }

    @Test
    @DisplayName("create(): closure after raceDate -> throws")
    void create_closureAfterRace() {
        createReq.raceDate = LocalDate.now().plusDays(5);
        createReq.registrationClosureDate = LocalDate.now().plusDays(6);
        assertThatThrownBy(() -> raceService.create(createReq))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("on or before the race date");
    }

    @Test
    @DisplayName("create(): ok when closure == raceDate")
    void create_closureEqualsRace_ok() {
        createReq.raceDate = LocalDate.now().plusDays(5);
        createReq.registrationClosureDate = createReq.raceDate;

        when(raceRepository.existsByTrackNameIgnoreCase("Spa")).thenReturn(false);
        when(raceRepository.save(any(Race.class))).thenAnswer(inv -> inv.getArgument(0));

        Race saved = raceService.create(createReq);
        assertThat(saved.getRegistrationClosureDate()).isEqualTo(createReq.raceDate);
    }

    @Test
    @DisplayName("update(): closure after raceDate -> throws")
    void update_closureAfterRace() {
        Race existing = new Race();
        existing.setId(1L);
        existing.setTrackName("Spa");
        when(raceRepository.findById(1L)).thenReturn(Optional.of(existing));

        updateReq.raceDate = LocalDate.now().plusDays(7);
        updateReq.registrationClosureDate = LocalDate.now().plusDays(8);

        assertThatThrownBy(() -> raceService.update(1L, updateReq))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("on or before the race date");
    }

    @Test
    @DisplayName("update(): ok when closure == raceDate")
    void update_closureEqualsRace_ok() {
        Race existing = new Race();
        existing.setId(1L);
        existing.setTrackName("Spa");

        when(raceRepository.findById(1L)).thenReturn(Optional.of(existing));
        // ❌ Removed unnecessary stubbing for existsByTrackNameIgnoreCase(..)
        when(raceRepository.save(any(Race.class))).thenAnswer(inv -> inv.getArgument(0));

        // keep same track name so uniqueness check is skipped (no unused stubs)
        updateReq.trackName = "Spa";
        updateReq.raceDate = LocalDate.now().plusDays(7);
        updateReq.registrationClosureDate = updateReq.raceDate;

        Race saved = raceService.update(1L, updateReq);

        assertThat(saved.getTrackName()).isEqualTo("Spa");
        assertThat(saved.getRegistrationClosureDate()).isEqualTo(updateReq.raceDate);
    }
}
