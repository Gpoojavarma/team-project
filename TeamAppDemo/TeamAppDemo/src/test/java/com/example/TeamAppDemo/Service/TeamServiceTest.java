
package com.example.TeamAppDemo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.TeamAppDemo.DTO.TeamDtos;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.Exception.BusinessRuleViolationException;
import com.example.TeamAppDemo.Exception.ResourceNotFoundException;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock private TeamRepository teamRepository;
    @Mock private DriverRaceRepository driverRaceRepository;

    @InjectMocks private TeamService teamService;

    private Team existingTeam;

    @BeforeEach
    void setup() {
        existingTeam = new Team();
        existingTeam.setId(1L);
        existingTeam.setName("Ferrari");
        existingTeam.setCity("Maranello");
        existingTeam.setCountry("Italy");
        existingTeam.setDescription("Scuderia Ferrari");
        existingTeam.setLogo(new byte[] {1,2,3});
    }

    // ---------- helpers ----------
    private MultipartFile logoPng(int sizeBytes) {
        byte[] bytes = new byte[sizeBytes];
        return new MockMultipartFile("logo", "logo.png", "image/png", bytes);
    }

    private MultipartFile logoJpeg(int sizeBytes) {
        byte[] bytes = new byte[sizeBytes];
        return new MockMultipartFile("logo", "logo.jpg", "image/jpeg", bytes);
    }

    // ---------- create ----------
    @Test
    void create_success_withPngLogo_savesTeam() throws IOException {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.teamName = "Mercedes-AMG";
        req.city = "Brackley";
        req.country = "UK";
        req.description = "Mercedes-AMG Petronas";

        when(teamRepository.existsByNameIgnoreCase("Mercedes-AMG")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });

        Team saved = teamService.create(req, logoPng(1024));

        assertEquals(2L, saved.getId());
        assertEquals("Mercedes-AMG", saved.getName());

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        assertArrayEquals(new byte[1024], captor.getValue().getLogo());
    }

    @Test
    void create_duplicateName_precheck_throwsBusinessRuleViolation() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.teamName = "Ferrari";
        when(teamRepository.existsByNameIgnoreCase("Ferrari")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> teamService.create(req, logoPng(100)));
        verify(teamRepository, never()).save(any());
    }

    @Test
    void create_logoIsNull_throwsBusinessRuleViolation() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.teamName = "AlphaTauri";
        when(teamRepository.existsByNameIgnoreCase("AlphaTauri")).thenReturn(false);

        assertThrows(BusinessRuleViolationException.class, () -> teamService.create(req, null));
    }

    @Test
    void create_logoIsEmpty_throwsBusinessRuleViolation() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.teamName = "AlphaTauri";
        when(teamRepository.existsByNameIgnoreCase("AlphaTauri")).thenReturn(false);

        MultipartFile empty = new MockMultipartFile("logo", "logo.png", "image/png", new byte[0]);
        assertThrows(BusinessRuleViolationException.class, () -> teamService.create(req, empty));
    }

    @Test
    void create_logoTooBig_throwsBusinessRuleViolation() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.teamName = "Haas";
        when(teamRepository.existsByNameIgnoreCase("Haas")).thenReturn(false);

        // 50 KB limit → use 51 KB
        assertThrows(BusinessRuleViolationException.class, () -> teamService.create(req, logoPng(51 * 1024)));
    }

    @Test
    void create_logoWrongType_throwsBusinessRuleViolation() {
        TeamDtos.TeamCreateRequest req = new TeamDtos.TeamCreateRequest();
        req.teamName = "Williams";
        when(teamRepository.existsByNameIgnoreCase("Williams")).thenReturn(false);

        MultipartFile gif = new MockMultipartFile("logo", "logo.gif", "image/gif", new byte[100]);
        assertThrows(BusinessRuleViolationException.class, () -> teamService.create(req, gif));
    }

    // ---------- update ----------
    @Test
    void update_success_savesChanges() {
        TeamDtos.TeamUpdateRequest req = new TeamDtos.TeamUpdateRequest();
        req.teamName = "Ferrari"; // unchanged
        req.city = "Maranello";
        req.country = "Italy";
        req.description = "Updated";

        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team updated = teamService.update(1L, req);

        assertEquals("Updated", updated.getDescription());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void update_nameChangedToExisting_throwsBusinessRuleViolation() {
        TeamDtos.TeamUpdateRequest req = new TeamDtos.TeamUpdateRequest();
        req.teamName = "Mercedes-AMG"; // new name
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByNameIgnoreCase("Mercedes-AMG")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> teamService.update(1L, req));
    }

    @Test
    void update_missingTeam_throwsNotFound() {
        TeamDtos.TeamUpdateRequest req = new TeamDtos.TeamUpdateRequest();
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.update(99L, req));
    }

    // ---------- updateLogo ----------
    @Test
    void updateLogo_success_persistsLogo() throws IOException {
        MultipartFile newLogo = logoJpeg(1024);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));

        teamService.updateLogo(1L, newLogo);

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        assertArrayEquals(new byte[1024], captor.getValue().getLogo());
    }

    @Test
    void updateLogo_missingTeam_throwsNotFound() {
        MultipartFile newLogo = logoPng(100);
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.updateLogo(99L, newLogo));
    }

    @Test
    void updateLogo_invalidLogo_throwsBusinessRuleViolation() {
        // Arrange: invalid logo that exceeds 50KB
        MultipartFile tooBig = logoPng(51 * 1024);

        // ❌ No stubbing of teamRepository.findById here — validation fails before any repository call.

        // Act + Assert
        assertThrows(BusinessRuleViolationException.class, () -> teamService.updateLogo(1L, tooBig));

        // Additionally ensure no repository interaction happened
        verify(teamRepository, never()).findById(anyLong());
        verify(teamRepository, never()).save(any());
        verifyNoInteractions(driverRaceRepository);
    }

    // ---------- get / getAll ----------
    @Test
    void get_existing_returnsTeam() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        Team t = teamService.get(1L);
        assertEquals(1L, t.getId());
    }

    @Test
    void get_missing_throwsNotFound() {
        when(teamRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> teamService.get(2L));
    }

    @Test
    void getAll_returnsTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(existingTeam));
        List<Team> teams = teamService.getAll();
        assertEquals(1, teams.size());
    }

    // ---------- delete ----------
    @Test
    void delete_whenTeamDriversRegisteredToRaces_throwsBusinessRuleViolation() {
        when(driverRaceRepository.countByDriver_Team_Id(1L)).thenReturn(1L);

        assertThrows(BusinessRuleViolationException.class, () -> teamService.delete(1L));
        verify(teamRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_whenNoRegistrations_deletes() {
        when(driverRaceRepository.countByDriver_Team_Id(1L)).thenReturn(0L);
        teamService.delete(1L);
        verify(teamRepository).deleteById(1L);
    }
}
