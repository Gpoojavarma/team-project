
package com.example.TeamAppDemo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.TeamAppDemo.Entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameIgnoreCase(String name);
    

}



