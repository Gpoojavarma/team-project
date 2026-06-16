package com.example.TeamAppDemo.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.TeamAppDemo.DTO.DriverDtos;
import com.example.TeamAppDemo.Entity.Driver;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.Exception.BusinessRuleViolationException;
import com.example.TeamAppDemo.Exception.ResourceNotFoundException;
import com.example.TeamAppDemo.Repository.DriverRaceRepository;
import com.example.TeamAppDemo.Repository.DriverRepository;
import com.example.TeamAppDemo.Repository.TeamRepository;

@Service
@Transactional
public class DriverService {
    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final DriverRaceRepository driverRaceRepository;

    public DriverService(DriverRepository driverRepository,
                         TeamRepository teamRepository,
                         DriverRaceRepository driverRaceRepository) {
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.driverRaceRepository = driverRaceRepository;
    }

private DriverDtos.DriverResponse toResponse(Driver d) {
        DriverDtos.DriverResponse r = new DriverDtos.DriverResponse();
        r.id = d.getId();
        r.firstName = d.getFirstName();
        r.lastName = d.getLastName();
        r.dateOfBirth = d.getDateOfBirth();
        r.teamId = (d.getTeam() != null ? d.getTeam().getId() : null);

        // (Optional) if Driver.driverRaces is mapped:
        r.raceIds = d.getDriverRaces().stream()
                     .map(dr -> dr.getRace().getId())
                     .toList();

        // ✅ Set the count via repository
        long cnt = driverRaceRepository.countByDriver_Id(d.getId());
        r.setRegistrationCount(cnt);

        return r;
    }

    public List<DriverDtos.DriverResponse> listResponses() {
        // Ensure team is fetched; if you also need races here, add them to EntityGraph.
        return driverRepository.findAll()
                               .stream()
                               .map(this::toResponse)
                               .toList();
    }


public DriverDtos.DriverResponse mapToResponse(Driver d) {
    return toResponse(d); // uses countByDriver_Id and sets registrationCount
}



public Driver create(DriverDtos.DriverCreateRequest req) {
    Driver d = new Driver();
    d.setFirstName(req.firstName);
    d.setLastName(req.lastName);
    d.setDateOfBirth(req.dateOfBirth);

    if (req.teamId != null) {
        Team t = teamRepository.findById(req.teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + req.teamId));
        d.setTeam(t);
    }

    Driver saved = driverRepository.save(d);

    

    return saved;
}



    public Driver update(Long id, DriverDtos.DriverUpdateRequest req) {
        Driver d = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        d.setFirstName(req.firstName);
        d.setLastName(req.lastName);
        d.setDateOfBirth(req.dateOfBirth);
        if (req.teamId != null) {
            Team t = teamRepository.findById(req.teamId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + req.teamId));
            d.setTeam(t);
        } else {
            d.setTeam(null);
        }
        return d;
    }

    public Driver get(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
    }

    public List<Driver> list() { 
        return driverRepository.findAll(); }

    public void delete(Long id) {
        if (driverRaceRepository.countByDriver_Id(id) > 0) {
            throw new BusinessRuleViolationException("Cannot delete driver: already registered to at least one race");
        }
        driverRepository.deleteById(id);
    }

    public Driver assignTeam(Long driverId, Long teamId) {
        Driver d = get(driverId);
        Team t = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        d.setTeam(t);
        return d;
    }
}





