
package com.example.TeamAppDemo.Entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DriverRaceId implements Serializable {
    private Long driverId;
    private Long raceId;

    public DriverRaceId() {}
    public DriverRaceId(Long driverId, Long raceId) {
        this.driverId = driverId;
        this.raceId = raceId;
    }

    public Long getDriverId() { return driverId; }
    public Long getRaceId() { return raceId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public void setRaceId(Long raceId) { this.raceId = raceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriverRaceId that)) return false;
        return Objects.equals(driverId, that.driverId) && Objects.equals(raceId, that.raceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId, raceId);
    }
}
