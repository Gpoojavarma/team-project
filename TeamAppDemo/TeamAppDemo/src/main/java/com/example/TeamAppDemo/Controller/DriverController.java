
package com.example.TeamAppDemo.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.TeamAppDemo.DTO.DriverDtos;
import com.example.TeamAppDemo.Entity.Driver;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Service.DriverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Drivers")
public class DriverController {

    private final DriverService driverService;
    private final DriverRaceRepository driverRaceRepository; // ✅ inject repo to compute count

    public DriverController(DriverService driverService,
                            DriverRaceRepository driverRaceRepository) { // ✅ add param
        this.driverService = driverService;
        this.driverRaceRepository = driverRaceRepository;               // ✅ assign
    }

    @Operation(summary="Create driver")
    @PostMapping
    public DriverDtos.DriverResponse create(@Valid @RequestBody DriverDtos.DriverCreateRequest req) {
        return toResponse(driverService.create(req));
    }

    @Operation(summary="Update driver")
    @PutMapping("/{id}")
    public DriverDtos.DriverResponse update(@PathVariable Long id,
                                            @Valid @RequestBody DriverDtos.DriverUpdateRequest req) {
        return toResponse(driverService.update(id, req));
    }

    @Operation(summary="Get driver")
    @GetMapping("/{id}")
    public DriverDtos.DriverResponse get(@PathVariable Long id) {
        return toResponse(driverService.get(id));
    }

    @Operation(summary="List drivers")
    @GetMapping
    public List<DriverDtos.DriverResponse> list() {
        // You can keep using the service version if you prefer,
        // but this works fine since toResponse() now sets the count.
        return driverService.list().stream().map(this::toResponse).toList();
    }

    @Operation(summary="Delete driver (blocked if registered in any race)")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        driverService.delete(id);
    }

    @Operation(summary="Assign driver to team")
    @PutMapping("/{id}/team/{teamId}")
    public DriverDtos.DriverResponse assignTeam(@PathVariable Long id, @PathVariable Long teamId) {
        return toResponse(driverService.assignTeam(id, teamId));
    }

    /** Map entity → DTO and set registrationCount */
    private DriverDtos.DriverResponse toResponse(Driver d) {
        DriverDtos.DriverResponse r = new DriverDtos.DriverResponse();
        r.id = d.getId();
        r.firstName = d.getFirstName();
        r.lastName = d.getLastName();
        r.dateOfBirth = d.getDateOfBirth();
        r.teamId = (d.getTeam() != null ? d.getTeam().getId() : null);
        r.raceIds = d.getDriverRaces().stream()
                     .map(dr -> dr.getRace().getId())
                     .toList();

        // ✅ Set registrations count for this driver
        long cnt = driverRaceRepository.countByDriver_Id(d.getId());
        r.setRegistrationCount(cnt);

        return r;
    }
}
