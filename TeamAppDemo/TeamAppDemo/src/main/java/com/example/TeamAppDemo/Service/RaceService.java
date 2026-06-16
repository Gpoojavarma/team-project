
package com.example.TeamAppDemo.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional
public class RaceService {
    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final DriverRaceRepository driverRaceRepository;

    public RaceService(RaceRepository raceRepository,
                       DriverRepository driverRepository,
                       DriverRaceRepository driverRaceRepository) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.driverRaceRepository = driverRaceRepository;
    }

    // --------- Create / Update ---------

    public Race create(RaceDtos.RaceCreateRequest req) {
        if (raceRepository.existsByTrackNameIgnoreCase(req.trackName)) {
            throw new BusinessRuleViolationException("Race track name must be unique");
        }

        validateRaceDates(req.raceDate, req.registrationClosureDate, /*isCreate*/ true);

        Race r = new Race();
        r.setTrackName(req.trackName);
        r.setCity(req.city);
        r.setCountry(req.country);
        r.setRaceDate(req.raceDate);
        r.setRegistrationClosureDate(req.registrationClosureDate);

        return raceRepository.save(r);
    }

    public Race update(Long id, RaceDtos.RaceUpdateRequest req) {
        Race r = raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found: " + id));

        if (!r.getTrackName().equalsIgnoreCase(req.trackName) &&
            raceRepository.existsByTrackNameIgnoreCase(req.trackName)) {
            throw new BusinessRuleViolationException("Race track name must be unique");
        }

        validateRaceDates(req.raceDate, req.registrationClosureDate, /*isCreate*/ false);

        r.setTrackName(req.trackName);
        r.setCity(req.city);
        r.setCountry(req.country);
        r.setRaceDate(req.raceDate);
        r.setRegistrationClosureDate(req.registrationClosureDate);

        // explicitly save for clarity and to ensure flush
        return raceRepository.save(r);
    }

    // --------- Queries ---------

    public Race get(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found: " + id));
    }

    public List<Race> list() {
        return raceRepository.findAll();
    }

    // --------- Delete ---------

    public void delete(Long id) {
        if (driverRaceRepository.countByRace_Id(id) > 0) {
            throw new BusinessRuleViolationException("Cannot delete race: at least one driver registered");
        }
        raceRepository.deleteById(id);
    }

    // --------- Registration ---------

 
public void registerDriver(Long raceId, Long driverId) {
    Race race = get(raceId);

    LocalDate today = LocalDate.now();

    // 1) Race date must be future
    if (!race.getRaceDate().isAfter(today)) {
        throw new BusinessRuleViolationException("Race date must be a future date.");
    }

    // 2) Optional closure date: registration is closed when today >= closure
    LocalDate closure = race.getRegistrationClosureDate();
    if (closure != null && (today.isEqual(closure) || today.isAfter(closure))) {
        // today >= closure date ⇒ closed
        throw new BusinessRuleViolationException("Registration closed for this race.");
    }

    Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

    DriverRaceId id = new DriverRaceId(driverId, raceId);
    if (driverRaceRepository.existsById(id)) {
        throw new BusinessRuleViolationException("Driver already registered to this race");
    }

    DriverRace dr = new DriverRace(driver, race);
    driverRaceRepository.save(dr);
}


    public void unregisterDriver(Long raceId, Long driverId) {
        DriverRaceId id = new DriverRaceId(driverId, raceId);
        DriverRace dr = driverRaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        driverRaceRepository.delete(dr);
    }

    // --------- Helpers / Validation ---------

    /**
     * Inclusive rule:
     * Registration is OPEN through the closure date and CLOSED starting the day AFTER.
     * CLOSED iff today > closureDate.
     * If closureDate is null, treat as OPEN.
     */
    public boolean isRegistrationClosed(LocalDate closureDate, LocalDate today) {
        if (closureDate == null) return false;
        if (today == null) today = LocalDate.now();
        return today.isAfter(closureDate);
    }

    /**
     * Validates race and closure dates against business rules.
     * - raceDate must be strictly in the future (matches @Future on the DTO).
     * - closureDate must be on/before raceDate.
     * - (Optional) For create, you may require closureDate to be today or later.
     */
    private void validateRaceDates(LocalDate raceDate, LocalDate closureDate, boolean isCreate) {
        LocalDate today = LocalDate.now();

        if (raceDate == null) {
            throw new BusinessRuleViolationException("Race date is mandatory.");
        }
        if (!raceDate.isAfter(today)) {
            throw new BusinessRuleViolationException("Race date must be in the future (after today).");
        }

        // closureDate may be optional. If it's optional in your DTO, keep this null-allowing branch.
        if (closureDate == null) {
            return;
        }

        // closure must be on or before race date
        if (closureDate.isAfter(raceDate)) {
            throw new BusinessRuleViolationException("Registration closure date must be on or before the race date.");
        }

        // If you want to disallow setting past closures during CREATE (but allow during UPDATE), enable this:
        // if (isCreate && closureDate.isBefore(today)) {
        //     throw new BusinessRuleViolationException("Registration closure date must be today or later.");
        // }
        //
        // If you want to disallow past closures for both create and update, remove the isCreate condition:
        // if (closureDate.isBefore(today)) {
        //     throw new BusinessRuleViolationException("Registration closure date must be today or later.");
        // }
        //
        // If you want to allow past closures always (e.g., backfilling data), leave as-is.
    }
}
