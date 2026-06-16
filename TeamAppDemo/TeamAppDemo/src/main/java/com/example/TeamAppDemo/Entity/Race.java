
package com.example.TeamAppDemo.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="races",
       uniqueConstraints = @UniqueConstraint(name="uk_race_track_name", columnNames = "track_name"))
public class Race {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="track_name", nullable=false, length=256)
    private String trackName;

    @Column(name="city", nullable=false, length=128)
    private String city;

    @Column(name="country", nullable=false, length=128)
    private String country;

    @Column(name="race_date", nullable=false)
    private LocalDate raceDate;

    @Column(name="registration_closure_date")
    private LocalDate registrationClosureDate;

    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DriverRace> driverRaces = new HashSet<>();

    public Long getId() { return id; }
    public String getTrackName() { return trackName; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public LocalDate getRaceDate() { return raceDate; }
    public LocalDate getRegistrationClosureDate() { return registrationClosureDate; }
    public Set<DriverRace> getDriverRaces() { return driverRaces; }

    public void setId(Long id) { this.id = id; }
    public void setTrackName(String trackName) { this.trackName = trackName; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setRaceDate(LocalDate raceDate) { this.raceDate = raceDate; }
    public void setRegistrationClosureDate(LocalDate registrationClosureDate) { this.registrationClosureDate = registrationClosureDate; }
    public void setDriverRaces(Set<DriverRace> driverRaces) {
        this.driverRaces = driverRaces;
    }
}
