
package com.example.TeamAppDemo.Entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class RaceEntityTest {

    @PersistenceContext
    EntityManager em;

    private Race baseRace(String track) {
        Race r = new Race();
        r.setTrackName(track);
        r.setCity("Monza");
        r.setCountry("Italy");
        r.setRaceDate(LocalDate.of(2028,9,6));
        r.setRegistrationClosureDate(LocalDate.of(2028,8,20));
        return r;
    }

    @Test
    void persistRace_ok() {
        Race r = baseRace("Monza");
        em.persist(r);
        em.flush();
        assertNotNull(r.getId());
    }

    @Test
    void uniqueTrackName_failsOnDuplicate() {
        em.persist(baseRace("UniqueTrack"));
        assertThrows(Exception.class, () -> {
            em.persist(baseRace("UniqueTrack"));
            em.flush();
        });
    }

    @Test
    void requiredColumnsNotNull() {
        Race r = new Race(); // missing trackName, city, country, raceDate
        assertThrows(Exception.class, () -> {
            em.persist(r);
            em.flush();
        });
    }

    @ParameterizedTest(name = "Race field {0} length {1} should fail")
    @CsvSource({
        "track,257",
        "city,129",
        "country,129"
    })
    void lengthConstraints_fail(String field, int len) {
        Race r = baseRace("LenOK");
        String big = "X".repeat(len);
        switch (field) {
            case "track" -> r.setTrackName(big);
            case "city" -> r.setCity(big);
            case "country" -> r.setCountry(big);
        }
        assertThrows(Exception.class, () -> {
            em.persist(r);
            em.flush();
        });
    }

    /* -------------------------------
       DriverRace tests from Race side
       ------------------------------- */
    @Nested
    class DriverRaceMappingFromRaceSide {

        @Test
        void addMultipleDriversToRace_persistedRace_cascadeAllWorks() {
            // parents first: persist drivers and race (ensure IDs exist)
            Driver d1 = new Driver(); d1.setFirstName("A"); d1.setLastName("L"); d1.setDateOfBirth(java.time.LocalDate.of(1990,1,1));
            em.persist(d1);
            Driver d2 = new Driver(); d2.setFirstName("B"); d2.setLastName("L"); d2.setDateOfBirth(java.time.LocalDate.of(1990,1,2));
            em.persist(d2);

            Race race = new Race(); race.setTrackName("Join-Track"); race.setCity("City"); race.setCountry("Country");
            race.setRaceDate(java.time.LocalDate.of(2030, 1, 1));
            em.persist(race);
            em.flush(); // <-- ensure ids are available

            DriverRace dr1 = new DriverRace(); dr1.setId(new DriverRaceId()); dr1.setDriver(d1); dr1.setRace(race);
            DriverRace dr2 = new DriverRace(); dr2.setId(new DriverRaceId()); dr2.setDriver(d2); dr2.setRace(race);
            race.getDriverRaces().add(dr1);
            race.getDriverRaces().add(dr2);

            // race is managed; cascade=ALL on Race.driverRaces will persist join rows on flush
            em.flush();
            em.clear();

            Long count = em.createQuery(
                    "select count(dr) from DriverRace dr where dr.race.trackName = :t", Long.class)
                .setParameter("t", "Join-Track")
                .getSingleResult();
            assertEquals(2L, count);
        }

        @Test
        void deletingRace_cascadesDeleteOfJoinRows() {
            Driver d = new Driver(); d.setFirstName("C"); d.setLastName("L"); d.setDateOfBirth(java.time.LocalDate.of(1990,1,3));
            em.persist(d);

            Race race = new Race(); race.setTrackName("Join-Delete"); race.setCity("City"); race.setCountry("Country");
            race.setRaceDate(java.time.LocalDate.of(2030, 2, 1));
            em.persist(race);
            em.flush();

            DriverRace dr = new DriverRace(); dr.setId(new DriverRaceId()); dr.setDriver(d); dr.setRace(race);
            race.getDriverRaces().add(dr);

            em.flush(); // persist join via cascade

            em.remove(race);
            em.flush();
            em.clear();

            Long count = em.createQuery("select count(dr) from DriverRace dr", Long.class).getSingleResult();
            assertEquals(0L, count);
        }
    }
}
