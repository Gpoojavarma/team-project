
package com.example.TeamAppDemo.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.TeamAppDemo.DTO.TeamDtos;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.Exception.BusinessRuleViolationException;
import com.example.TeamAppDemo.Exception.ResourceNotFoundException;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Repository.TeamRepository;

@Service
@Transactional
public class TeamService {

    private static final long MAX_LOGO_BYTES = 50 * 1024; // 50KB
    private static final Set<String> ALLOWED_LOGO_TYPES = Set.of("image/png", "image/jpeg");

    private final TeamRepository teamRepository;
    private final DriverRaceRepository driverRaceRepository;

    public TeamService(TeamRepository teamRepository, DriverRaceRepository driverRaceRepository) {
        this.teamRepository = teamRepository;
        this.driverRaceRepository = driverRaceRepository;
    }

    public Team create(TeamDtos.TeamCreateRequest req, MultipartFile logo) throws IOException {
        if (teamRepository.existsByNameIgnoreCase(req.teamName)) {
            throw new BusinessRuleViolationException("Team name must be unique");
        }

        validateLogoFile(logo);

        Team team = new Team();
        team.setName(req.teamName);
        team.setCity(req.city);
        team.setCountry(req.country);
        team.setDescription(req.description);
        team.setLogo(logo.getBytes());

        return teamRepository.save(team);
    }

    public Team update(Long id, TeamDtos.TeamUpdateRequest req) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));

        String currentName = team.getName() == null ? "" : team.getName();
        String newName = req.teamName == null ? "" : req.teamName;

        // If the incoming name differs, enforce uniqueness
        if (!currentName.equalsIgnoreCase(newName) &&
            teamRepository.existsByNameIgnoreCase(newName)) {
            throw new BusinessRuleViolationException("Team name must be unique");
        }

        team.setName(req.teamName);
        team.setCity(req.city);
        team.setCountry(req.country);
        team.setDescription(req.description);

        // Explicitly persist and return the saved entity (ensures changes are flushed)
        return teamRepository.save(team);
    }

    public void updateLogo(Long id, MultipartFile logo) throws IOException {
        validateLogoFile(logo);

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));

        team.setLogo(logo.getBytes());

        // ✅ Persist the change explicitly
        teamRepository.save(team);
    }

    public Team get(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    public List<Team> getAll() {
        return teamRepository.findAll();
    }

    public void delete(Long id) {
        if (driverRaceRepository.countByDriver_Team_Id(id) > 0) {
            throw new BusinessRuleViolationException(
                "Cannot delete team: one or more drivers in this team are registered to races"
            );
        }
        teamRepository.deleteById(id);
    }

    // --- helpers ---
    private void validateLogoFile(MultipartFile logo) {
        if (logo == null || logo.isEmpty()) {
            throw new BusinessRuleViolationException("Team logo is mandatory");
        }
        if (logo.getSize() > MAX_LOGO_BYTES) {
            throw new BusinessRuleViolationException("Team logo must be ≤ 50KB");
        }
        String ct = logo.getContentType();
        if (ct == null || !ALLOWED_LOGO_TYPES.contains(ct.toLowerCase())) {
            throw new BusinessRuleViolationException("Team logo must be PNG or JPEG");
        }
    }
}
