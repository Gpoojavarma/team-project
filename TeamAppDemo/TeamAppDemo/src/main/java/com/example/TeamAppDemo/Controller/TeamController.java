
package com.example.TeamAppDemo.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // ✅ add this for getLogo
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.TeamAppDemo.DTO.TeamDtos;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.Service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) { this.teamService = teamService; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create team (multipart with logo ≤50KB)")
    public TeamDtos.TeamResponse create(
            @Valid
            @Schema(
                type = "string",
                example = "{\"teamName\":\"string\",\"city\":\"string\",\"country\":\"string\",\"description\":\"string\"}"
            )
            @RequestPart("request") String requestJson, // accept JSON as string
            @Schema(type = "string", format = "binary")
            @RequestPart("logo") MultipartFile logo
    ) throws IOException {

        // Convert JSON string to DTO
        TeamDtos.TeamCreateRequest request =
            new com.fasterxml.jackson.databind.ObjectMapper().readValue(requestJson, TeamDtos.TeamCreateRequest.class);

        Team t = teamService.create(request, logo);
        return toResponse(t);
    }

    @PutMapping("/{id}")
    public TeamDtos.TeamResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TeamDtos.TeamUpdateRequest request
    ) {
        return toResponse(teamService.update(id, request));
    }

    @PutMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateLogo(
            @PathVariable Long id,
            @Schema(type = "string", format = "binary")
            @RequestPart("logo") MultipartFile logo
    ) throws IOException {
        teamService.updateLogo(id, logo);
    }

    @Operation(summary = "Get team by id")
    @GetMapping("/{id}")
    public TeamDtos.TeamResponse getTeam(@PathVariable Long id) {
        return toResponse(teamService.get(id));
    }

    @Operation(summary = "List all teams")
    @GetMapping
    public List<TeamDtos.TeamResponse> list() {
        return teamService
                .getAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(summary = "Delete team (blocked if any driver registered to any race)")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { teamService.delete(id); }

    // ✅ Helper stays INSIDE the class
    private TeamDtos.TeamResponse toResponse(Team t) {
        TeamDtos.TeamResponse r = new TeamDtos.TeamResponse();
        r.id = t.getId();
        r.teamName = t.getName();
        r.city = t.getCity();
        r.country = t.getCountry();
        r.description = t.getDescription();
        r.logoPresent = t.getLogo() != null && t.getLogo().length > 0;
        r.driverIds = t.getDrivers().stream().map(d -> d.getId()).toList();

        // ✅ expose logo URL for the UI (relative path works with your baseURL)
        r.logoUrl = r.logoPresent ? ("/api/teams/" + t.getId() + "/logo") : null;
        
        return r;
    }

    // ✅ Logo streaming endpoint INSIDE the class
    @GetMapping("/{id}/logo")
    @Operation(summary = "Get team logo image")
    public ResponseEntity<byte[]> getLogo(@PathVariable Long id) {
        Team t = teamService.get(id);
        byte[] logo = t.getLogo();
        if (logo == null || logo.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // If you persist the real MIME type (png/jpg), return that here.
        // For now default to PNG.
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(logo);
    }
}

