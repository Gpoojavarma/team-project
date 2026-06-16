
package com.example.TeamAppDemo.Controller;

import com.example.TeamAppDemo.DTO.RaceDtos;
import com.example.TeamAppDemo.Entity.Race;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.TeamAppDemo.Service.RaceService;

import java.util.List;

@RestController
@RequestMapping("/api/races")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Races")
public class RaceController {

 private final RaceService raceService;

    public RaceController(RaceService raceService) {
         this.raceService = raceService; }

    @Operation(summary="Create race")
    @PostMapping
    public RaceDtos.RaceResponse create(@Valid @RequestBody RaceDtos.RaceCreateRequest req) {
        return toResponse(raceService.create(req));
    }

    @Operation(summary="Update race")
    @PutMapping("/{id}")
    public RaceDtos.RaceResponse update(@PathVariable Long id, @Valid @RequestBody RaceDtos.RaceUpdateRequest req) {
        return toResponse(raceService.update(id, req));
    }

    @Operation(summary="Get race")
    @GetMapping("/{id}")
    public RaceDtos.RaceResponse get(@PathVariable Long id) {
        return toResponse(raceService.get(id));
    }

    @Operation(summary="List races")
    @GetMapping
    public List<RaceDtos.RaceResponse> list() {
        return raceService.list().stream().map(this::toResponse).toList();
    }

    @Operation(summary="Delete race (blocked if any driver registered)")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { raceService.delete(id); }

    @Operation(summary="Register driver to race")
    @PostMapping("/{raceId}/registrations/{driverId}")
    public void register(@PathVariable Long raceId, @PathVariable Long driverId) {
        raceService.registerDriver(raceId, driverId);
    }

    @Operation(summary="Unregister driver from race")
    @DeleteMapping("/{raceId}/registrations/{driverId}")
    public void unregister(@PathVariable Long raceId, @PathVariable Long driverId) {
        raceService.unregisterDriver(raceId, driverId);
    }

    private RaceDtos.RaceResponse toResponse(Race r) {
        RaceDtos.RaceResponse res = new RaceDtos.RaceResponse();
        res.id = r.getId();
        res.trackName = r.getTrackName();
        res.city = r.getCity();
        res.country = r.getCountry();
        res.raceDate = r.getRaceDate();
        res.registrationClosureDate = r.getRegistrationClosureDate();
        res.registeredDriverIds = r.getDriverRaces().stream().map(dr -> dr.getDriver().getId()).toList();
        return res;
    }
}
