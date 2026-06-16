package com.example.TeamAppDemo.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.TeamAppDemo.DTO.DriverDtos;
import com.example.TeamAppDemo.Entity.Driver;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.Exception.BusinessRuleViolationException;
import com.example.TeamAppDemo.Exception.ResourceNotFoundException;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Repository.DriverRepository;
import com.example.TeamAppDemo.Repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock private DriverRepository driverRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private DriverRaceRepository driverRaceRepository;

    @InjectMocks private DriverService driverService;

    private Driver existingDriver;
    private Team existingTeam;

    @BeforeEach
    void setup() {
        existingDriver = new Driver();
        existingDriver.setId(1L);
        existingDriver.setFirstName("Lewis");
        existingDriver.setLastName("Hamilton");
        existingDriver.setDateOfBirth(LocalDate.of(1985, 1, 7));

        existingTeam = new Team();
        existingTeam.setId(10L);
        existingTeam.setName("Mercedes-AMG");
    }

    // -------- create --------
    @Test
    void create_withoutTeam_savesDriver() {
        DriverDtos.DriverCreateRequest req = new DriverDtos.DriverCreateRequest();
        req.firstName = "Max";
        req.lastName = "Verstappen";
        req.dateOfBirth = LocalDate.of(1997, 9, 30);
        req.teamId = null;

        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> {
            Driver saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Driver result = driverService.create(req);

        assertEquals(99L, result.getId());
        assertEquals("Max", result.getFirstName());
        assertNull(result.getTeam());

        // verify saved entity values
        ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(captor.capture());
        Driver toSave = captor.getValue();
        assertEquals("Verstappen", toSave.getLastName());
        assertNull(toSave.getTeam());
    }

    @Test
    void create_withTeam_attachesTeamAndSaves() {
        DriverDtos.DriverCreateRequest req = new DriverDtos.DriverCreateRequest();
        req.firstName = "Charles";
        req.lastName = "Leclerc";
        req.dateOfBirth = LocalDate.of(1997, 10, 16);
        req.teamId = 10L;

        when(teamRepository.findById(10L)).thenReturn(Optional.of(existingTeam));
        when(driverRepository.save(any())).thenAnswer(inv -> {
            Driver saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Driver result = driverService.create(req);

        assertEquals(100L, result.getId());
        assertNotNull(result.getTeam());
        assertEquals(10L, result.getTeam().getId());
        verify(teamRepository).findById(10L);
        verify(driverRepository).save(any());
    }

    @Test
    void create_withUnknownTeam_throwsNotFound() {
        DriverDtos.DriverCreateRequest req = new DriverDtos.DriverCreateRequest();
        req.firstName = "Lando";
        req.lastName = "Norris";
        req.dateOfBirth = LocalDate.of(1999, 11, 13);
        req.teamId = 404L;

        when(teamRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.create(req));
        verify(driverRepository, never()).save(any());
    }

    // -------- update --------
    @Test
    void update_withNewTeam_updatesFieldsAndTeam_noSaveCall() {
        DriverDtos.DriverUpdateRequest req = new DriverDtos.DriverUpdateRequest();
        req.firstName = "Lewis";
        req.lastName = "Hamilton";
        req.dateOfBirth = LocalDate.of(1985, 1, 7);
        req.teamId = 10L;

        when(driverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(existingTeam));

        Driver updated = driverService.update(1L, req);

        assertEquals("Lewis", updated.getFirstName());
        assertEquals("Hamilton", updated.getLastName());
        assertEquals(LocalDate.of(1985, 1, 7), updated.getDateOfBirth());
        assertEquals(10L, updated.getTeam().getId());

        // In current service, update() returns entity without saving.
        verify(driverRepository, never()).save(any());
    }

    @Test
    void update_withNullTeamId_clearsTeam() {
        existingDriver.setTeam(existingTeam);

        DriverDtos.DriverUpdateRequest req = new DriverDtos.DriverUpdateRequest();
        req.firstName = "Lewis";
        req.lastName = "Hamilton";
        req.dateOfBirth = LocalDate.of(1985, 1, 7);
        req.teamId = null;

        when(driverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));

        Driver updated = driverService.update(1L, req);

        assertNull(updated.getTeam());
        verify(teamRepository, never()).findById(any());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void update_unknownDriver_throwsNotFound() {
        DriverDtos.DriverUpdateRequest req = new DriverDtos.DriverUpdateRequest();
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.update(99L, req));
    }

    @Test
    void update_teamIdUnknown_throwsNotFound() {
        DriverDtos.DriverUpdateRequest req = new DriverDtos.DriverUpdateRequest();
        req.teamId = 77L;

        when(driverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(teamRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.update(1L, req));
    }

    // -------- get/list --------
    @Test
    void get_existing_returnsDriver() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));

        Driver d = driverService.get(1L);

        assertEquals(1L, d.getId());
        verify(driverRepository).findById(1L);
    }

    @Test
    void get_missing_throwsNotFound() {
        when(driverRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.get(2L));
    }

    @Test
    void list_returnsAll() {
        when(driverRepository.findAll()).thenReturn(List.of(existingDriver));

        List<Driver> drivers = driverService.list();

        assertEquals(1, drivers.size());
        verify(driverRepository).findAll();
    }

    // -------- delete --------
    @Test
    void delete_whenRegisteredToRace_throwsBusinessRuleViolation() {
        when(driverRaceRepository.countByDriver_Id(1L)).thenReturn(1L);

        assertThrows(BusinessRuleViolationException.class, () -> driverService.delete(1L));

        verify(driverRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_whenNotRegistered_deletes() {
        when(driverRaceRepository.countByDriver_Id(1L)).thenReturn(0L);

        driverService.delete(1L);

        verify(driverRepository).deleteById(1L);
    }

    // -------- assignTeam --------
    @Test
    void assignTeam_setsTeamOnDriver_noSaveCall() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(existingTeam));

        Driver d = driverService.assignTeam(1L, 10L);

        assertEquals(10L, d.getTeam().getId());
        verify(driverRepository).findById(1L);
        verify(teamRepository).findById(10L);
        verify(driverRepository, never()).save(any());
    }

    @Test
    void assignTeam_unknownDriver_throwsNotFound() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.assignTeam(99L, 10L));
    }

    @Test
    void assignTeam_unknownTeam_throwsNotFound() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(teamRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.assignTeam(1L, 404L));
    }
}
