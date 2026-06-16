
package com.example.TeamAppDemo.Repository;

import com.example.TeamAppDemo.Entity.DriverRace;
import com.example.TeamAppDemo.Entity.DriverRaceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRaceRepository extends JpaRepository<DriverRace, DriverRaceId> {
    long countByDriver_Id(Long driverId);
    long countByRace_Id(Long raceId);
    long countByDriver_Team_Id(Long teamId);
    boolean existsById(DriverRaceId id);
}
