
package com.example.TeamAppDemo.Entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name="driver_race")
public class DriverRace {
    @EmbeddedId
    private DriverRaceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("driverId")
    @JoinColumn(name="driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("raceId")
    @JoinColumn(name="race_id")
    private Race race;

    @Column(name="registered_at", nullable=false)
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    public DriverRace() {}
    public DriverRace(Driver driver, Race race) {
        this.driver = driver;
        this.race = race;
        this.id = new DriverRaceId(driver.getId(), race.getId());
    }

    public DriverRaceId getId() { return id; }
    public Driver getDriver() { return driver; }
    public Race getRace() { return race; }
    public OffsetDateTime getRegisteredAt() { return registeredAt; }

    public void setId(DriverRaceId id) { this.id = id; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public void setRace(Race race) { this.race = race; }
    public void setRegisteredAt(OffsetDateTime registeredAt) { this.registeredAt = registeredAt; }
}
