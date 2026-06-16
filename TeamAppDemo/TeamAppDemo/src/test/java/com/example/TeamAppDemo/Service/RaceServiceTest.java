
package com.example.TeamAppDemo.Service;

import com.example.TeamAppDemo.DTO.RaceDtos;
import com.example.TeamAppDemo.Entity.Driver;
import com.example.TeamAppDemo.Entity.DriverRace;
import com.example.TeamAppDemo.Entity.DriverRaceId;
import com.example.TeamAppDemo.Entity.Race;
import com.example.TeamAppDemo.Exception.BusinessRuleViolationException;
import com.example.TeamAppDemo.Exception.ResourceNotFoundException;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Repository.DriverRepository;
import com.example.TeamAppDemo.Repository.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaceServiceTest {

    @Mock private RaceRepository raceRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private DriverRaceRepository driverRaceRepository;

    @InjectMocks private RaceService raceService;

    private RaceDtos.RaceCreateRequest createReq;
    private RaceDtos.RaceUpdateRequest updateReq;

    @BeforeEach
    void setUp() {
        createReq = new RaceDtos.RaceCreateRequest();
        createReq.trackName = "Monza";
        createReq.city = "Monza";
        createReq.country = "Italy";
        createReq.raceDate = LocalDate.now().plusDays(10);
        createReq.registrationClosureDate = LocalDate.now(); // valid due to @PastOrPresent

        updateReq = new RaceDtos.RaceUpdateRequest();
        updateReq.trackName = "Monza Updated";
        updateReq.city = "Monza";
        updateReq.country = "Italy";
        updateReq.raceDate = LocalDate.now().plusDays(20);
        updateReq.registrationClosureDate = LocalDate.now(); // valid due to @PastOrPresent
    }

    @Test
    @DisplayName("create(): saves race when unique and dates valid")
    void create_saves() {
        when(raceRepository.existsByTrackNameIgnoreCase("Monza")).thenReturn(false);
        when(raceRepository.save(any(Race.class))).thenAnswer(inv -> {
            Race r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        Race result = raceService.create(createReq);

        ArgumentCaptor<Race> captor = ArgumentCaptor.forClass(Race.class);
        verify(raceRepository).save(captor.capture());
        Race saved = captor.getValue();

        assertThat(saved.getTrackName()).isEqualTo("Monza");
        assertThat(saved.getRaceDate()).isEqualTo(createReq.raceDate);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create(): duplicate track throws")
    void create_duplicate_throws() {
        when(raceRepository.existsByTrackNameIgnoreCase("Monza")).thenReturn(true);
        assertThatThrownBy(() -> raceService.create(createReq))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("unique");
        verify(raceRepository, never()).save(any());
    }

    @Test
    @DisplayName("update(): updates existing race when valid")
    void update_valid_saves() {
        Race existing = new Race();
        existing.setId(7L);
        existing.setTrackName("Old Monza");
        when(raceRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(raceRepository.existsByTrackNameIgnoreCase("Monza Updated")).thenReturn(false);
        when(raceRepository.save(any(Race.class))).thenAnswer(inv -> inv.getArgument(0));

        Race updated = raceService.update(7L, updateReq);

        assertThat(updated.getTrackName()).isEqualTo("Monza Updated");
        assertThat(updated.getRaceDate()).isEqualTo(updateReq.raceDate);
    }

    @Test
    @DisplayName("update(): missing race throws 404")
    void update_notFound_throws404() {
        when(raceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> raceService.update(99L, updateReq))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Race not found");
    }

    @Test
    @DisplayName("get(): returns race by id or throws 404")
    void get_byId() {
        Race r = new Race();
        r.setId(2L);
        when(raceRepository.findById(2L)).thenReturn(Optional.of(r));
        assertThat(raceService.get(2L)).isEqualTo(r);

        when(raceRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> raceService.get(3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("list(): returns all")
    void list_all() {
        when(raceRepository.findAll()).thenReturn(List.of(new Race(), new Race()));
        assertThat(raceService.list()).hasSize(2);
    }

    @Test
    @DisplayName("delete(): blocks when registrations exist")
    void delete_blocked_ifRegistered() {
        when(driverRaceRepository.countByRace_Id(5L)).thenReturn(1L);
        assertThatThrownBy(() -> raceService.delete(5L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot delete race");
        verify(raceRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete(): calls deleteById when no registrations")
    void delete_whenExists_deletesById() {
        when(driverRaceRepository.countByRace_Id(6L)).thenReturn(0L);
        raceService.delete(6L);
        verify(raceRepository).deleteById(6L);
        verifyNoMoreInteractions(raceRepository);
    }

    @Test
    @DisplayName("registerDriver(): success when future race and open (closure null)")
    void registerDriver_open_saves() {
        long raceId = 10L, driverId = 20L;
        Race race = new Race();
        race.setId(raceId);
        race.setRaceDate(LocalDate.now().plusDays(3));
        race.setRegistrationClosureDate(null); // open deterministically
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        Driver driver = new Driver();
        driver.setId(driverId);
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));

        when(driverRaceRepository.existsById(any(DriverRaceId.class))).thenReturn(false);

        raceService.registerDriver(raceId, driverId);
        verify(driverRaceRepository).save(any(DriverRace.class));
    }

    @Test
    @DisplayName("registerDriver(): throws when race date is not future")
    void registerDriver_raceNotFuture_throws() {
        long raceId = 11L;
        Race race = new Race();
        race.setId(raceId);
        race.setRaceDate(LocalDate.now()); // not strictly future
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        assertThatThrownBy(() -> raceService.registerDriver(raceId, 200L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("future");
    }

    @Test
    @DisplayName("registerDriver(): throws when registration closed (today >= closure)")
    void registerDriver_closed_throws() {
        long raceId = 12L;
        Race race = new Race();
        race.setId(raceId);
        race.setRaceDate(LocalDate.now().plusDays(5));
        race.setRegistrationClosureDate(LocalDate.now()); // closed today per service rule
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        assertThatThrownBy(() -> raceService.registerDriver(raceId, 300L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("closed");
    }

    @Test
    @DisplayName("registerDriver(): throws 404 when driver missing (and registration open)")
    void registerDriver_unknownDriver_throws404() {
        long raceId = 13L, driverId = 400L;
        Race race = new Race();
        race.setId(raceId);
        race.setRaceDate(LocalDate.now().plusDays(5));
        race.setRegistrationClosureDate(null); // open
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> raceService.registerDriver(raceId, driverId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Driver not found");
    }

    @Test
    @DisplayName("registerDriver(): throws when already registered")
    void registerDriver_duplicate_throws() {
        long raceId = 14L, driverId = 500L;
        Race race = new Race();
        race.setId(raceId);
        race.setRaceDate(LocalDate.now().plusDays(5));
        race.setRegistrationClosureDate(null); // open
        when(raceRepository.findById(raceId)).thenReturn(Optional.of(race));

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(new Driver()));
        when(driverRaceRepository.existsById(any(DriverRaceId.class))).thenReturn(true);

        assertThatThrownBy(() -> raceService.registerDriver(raceId, driverId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("unregisterDriver(): deletes registration when exists")
    void unregisterDriver_deletes() {
        long raceId = 15L, driverId = 600L;
        DriverRaceId id = new DriverRaceId(driverId, raceId);
        when(driverRaceRepository.findById(id)).thenReturn(Optional.of(new DriverRace()));
        raceService.unregisterDriver(raceId, driverId);
        verify(driverRaceRepository).delete(any(DriverRace.class));
    }

    @Test
    @DisplayName("unregisterDriver(): throws 404 when registration not found")
    void unregisterDriver_notFound_throws404() {
        long raceId = 16L, driverId = 700L;
        DriverRaceId id = new DriverRaceId(driverId, raceId);
        when(driverRaceRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> raceService.unregisterDriver(raceId, driverId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Registration not found");
    }

    @Test
    @DisplayName("isRegistrationClosed(): closed only when today > closure")
    void isRegistrationClosed_rule() {
        LocalDate today = LocalDate.now();
        assertThat(raceService.isRegistrationClosed(null, today)).isFalse();
        assertThat(raceService.isRegistrationClosed(today, today)).isFalse(); // open through closure day
        assertThat(raceService.isRegistrationClosed(today.minusDays(1), today)).isTrue(); // today > closure
    }
}
