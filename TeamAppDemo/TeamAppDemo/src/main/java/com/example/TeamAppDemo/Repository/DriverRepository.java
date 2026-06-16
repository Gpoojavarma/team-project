
package com.example.TeamAppDemo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.TeamAppDemo.Entity.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    // Ensures team is loaded with drivers in a single query
    @EntityGraph(attributePaths = {"team"})
    List<Driver> findAll();

}
