
package com.example.TeamAppDemo.Entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class DriverEntityTest {

    @PersistenceContext
    EntityManager em;

    private Driver baseDriver() {
        Driver d = new Driver();
        d.setFirstName("Ada");
        d.setLastName("Lovelace");
        d.setDateOfBirth(LocalDate.of(1815,12,10));
        return d;
    }

    private Team baseTeam(String name) {
        Team t = new Team();
        t.setName(name);
        t.setCity("Gurgaon");
        t.setCountry("India");
        t.setLogo(new byte[]{1});
        return t;
    }

    @Test
    void persistDriver_ok() {
        Driver d = baseDriver();
        em.persist(d);
        em.flush();
        assertNotNull(d.getId());
    }

    @Test
    void requiredColumnsMustNotBeNull() {
        Driver d = new Driver(); // missing firstName, lastName, dateOfBirth
        assertThrows(Exception.class, () -> {
            em.persist(d);
            em.flush();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {97, 120})
    void firstNameTooLong_fails(int len) {
        Driver d = baseDriver();
        d.setFirstName("X".repeat(len)); // > 96
        assertThrows(Exception.class, () -> {
            em.persist(d);
            em.flush();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {97, 150})
    void lastNameTooLong_fails(int len) {
        Driver d = baseDriver();
        d.setLastName("X".repeat(len)); // > 96
        assertThrows(Exception.class, () -> {
            em.persist(d);
            em.flush();
        });
    }

    @Test
    void driverBelongsToTeam_mappingWorks() {
        Team team = baseTeam("Team-1");
        em.persist(team);
        em.flush(); // ensure team id assigned

        Driver d = baseDriver();
        d.setTeam(team);
        em.persist(d);
        em.flush();
        em.clear();

        Driver back = em.find(Driver.class, d.getId());
        assertNotNull(back.getTeam());
        assertEquals(team.getId(), back.getTeam().getId());
    }

    /* -------------------------------
       DriverRace tests from Driver side
       ------------------------------- */
    @Nested
    class DriverRaceMappingFromDriverSide {

        @Test
        void cascadePersist_driverRaces_savedWhenDriverIsSaved() {
            // Persist parents & assign IDs first (MapsId requires them)
            Race r1 = new Race(); r1.setTrackName("Join-1"); r1.setCity("City"); r1.setCountry("Country");
            r1.setRaceDate(java.time.LocalDate.of(2030, 1, 1));
            em.persist(r1);
            Race r2 = new Race(); r2.setTrackName("Join-2"); r2.setCity("City"); r2.setCountry("Country");
            r2.setRaceDate(java.time.LocalDate.of(2030, 1, 2));
            em.persist(r2);

            Driver d = new Driver(); d.setFirstName("D1"); d.setLastName("L"); d.setDateOfBirth(java.time.LocalDate.of(1990,1,1));
            em.persist(d);
            em.flush(); // <-- ensure d/r have IDs before creating DriverRace

            DriverRace dr1 = new DriverRace(); dr1.setId(new DriverRaceId()); dr1.setDriver(d); dr1.setRace(r1);
            DriverRace dr2 = new DriverRace(); dr2.setId(new DriverRaceId()); dr2.setDriver(d); dr2.setRace(r2);
            d.getDriverRaces().add(dr1);
            d.getDriverRaces().add(dr2);

            // d is managed; cascade=ALL on Driver.driverRaces will persist join rows on flush
            em.flush();
            em.clear();

            Long count = em.createQuery("select count(dr) from DriverRace dr", Long.class).getSingleResult();
            assertEquals(2L, count);
        }

        @Test
        void orphanRemoval_removingJoinFromDriversSet_deletesRow() {
            Race r = new Race(); r.setTrackName("Join-X"); r.setCity("City"); r.setCountry("Country");
            r.setRaceDate(java.time.LocalDate.of(2030, 2, 1));
            em.persist(r);

            Driver d = new Driver(); d.setFirstName("D2"); d.setLastName("L"); d.setDateOfBirth(java.time.LocalDate.of(1990,1,2));
            em.persist(d);
            em.flush();

            DriverRace dr = new DriverRace(); dr.setId(new DriverRaceId()); dr.setDriver(d); dr.setRace(r);
            d.getDriverRaces().add(dr);

            em.flush(); // persist join via cascade

            d.getDriverRaces().remove(dr); // orphanRemoval = true on Driver.driverRaces
            em.flush();
            em.clear();

            Long count = em.createQuery("select count(dr) from DriverRace dr", Long.class).getSingleResult();
            assertEquals(0L, count);
        }

        @Test
        void deletingDriver_cascadesDeleteToJoinRows() {
            Race r = new Race(); r.setTrackName("Join-Y"); r.setCity("City"); r.setCountry("Country");
            r.setRaceDate(java.time.LocalDate.of(2030, 3, 1));
            em.persist(r);

            Driver d = new Driver(); d.setFirstName("D3"); d.setLastName("L"); d.setDateOfBirth(java.time.LocalDate.of(1990,1,3));
            em.persist(d);
            em.flush();

            DriverRace dr = new DriverRace(); dr.setId(new DriverRaceId()); dr.setDriver(d); dr.setRace(r);
            d.getDriverRaces().add(dr);

            em.flush(); // persist join via cascade

            em.remove(d);
            em.flush();
            em.clear();

            Long count = em.createQuery("select count(dr) from DriverRace dr", Long.class).getSingleResult();
            assertEquals(0L, count);
        }

        @Test
        void duplicateDriverRaceForSamePair_violatesCompositePk() {
            Race r = new Race(); r.setTrackName("Join-Z"); r.setCity("City"); r.setCountry("Country");
            r.setRaceDate(java.time.LocalDate.of(2030, 4, 1));
            em.persist(r);

            Driver d = new Driver(); d.setFirstName("D4"); d.setLastName("L"); d.setDateOfBirth(java.time.LocalDate.of(1990,1,4));
            em.persist(d);
            em.flush();

            DriverRace dr1 = new DriverRace(); dr1.setId(new DriverRaceId()); dr1.setDriver(d); dr1.setRace(r);
            DriverRace dr2 = new DriverRace(); dr2.setId(new DriverRaceId()); dr2.setDriver(d); dr2.setRace(r); // same pair

            d.getDriverRaces().add(dr1);
            d.getDriverRaces().add(dr2);

            assertThrows(Exception.class, () -> {
                em.flush(); // should fail on duplicate composite PK (driverId, raceId)
            });
        }

        @Test
        void mapsId_setsCompositeId_andRegisteredAtIsNotNull() {
            Race r = new Race(); r.setTrackName("Join-Id"); r.setCity("City"); r.setCountry("Country");
            r.setRaceDate(java.time.LocalDate.of(2030, 5, 1));
            em.persist(r);

            Driver d = new Driver(); d.setFirstName("D5"); d.setLastName("L"); d.setDateOfBirth(java.time.LocalDate.of(1990,1,5));
            em.persist(d);
            em.flush();

            DriverRace dr = new DriverRace(); dr.setId(new DriverRaceId()); dr.setDriver(d); dr.setRace(r);
            d.getDriverRaces().add(dr);

            em.flush();
            em.clear();

            DriverRace persisted = em.createQuery(
                "select dr from DriverRace dr where dr.race.trackName = :t", DriverRace.class)
                .setParameter("t", "Join-Id")
                .getSingleResult();

            assertNotNull(persisted.getId());
            assertEquals(persisted.getDriver().getId(), persisted.getId().getDriverId());
            assertEquals(persisted.getRace().getId(), persisted.getId().getRaceId());
            assertNotNull(persisted.getRegisteredAt());
        }
    }
}

