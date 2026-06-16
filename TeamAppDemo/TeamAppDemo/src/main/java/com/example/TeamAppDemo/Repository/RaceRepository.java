
package com.example.TeamAppDemo.Repository;

import com.example.TeamAppDemo.Entity.Race;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceRepository extends JpaRepository<Race, Long> {
    boolean existsByTrackNameIgnoreCase(String trackName);
}
