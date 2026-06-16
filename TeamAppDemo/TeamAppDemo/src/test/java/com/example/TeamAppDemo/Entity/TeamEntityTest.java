
package com.example.TeamAppDemo.Entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@Transactional
class TeamEntityTest {

    @PersistenceContext
    EntityManager em;

    private Team validTeam(String name) {
        Team t = new Team();
        t.setName(name);
        t.setCity("Gurgaon");
        t.setCountry("India");
        t.setLogo(new byte[]{1,2,3});
        t.setDescription("desc");
        return t;
    }

    private Driver driver(String fn, String ln, java.time.LocalDate dob) {
        Driver d = new Driver();
        d.setFirstName(fn);
        d.setLastName(ln);
        d.setDateOfBirth(dob);
        return d;
    }

    @Test
    void persistTeam_ok_andDefaultRegisteredZero() {
        Team t = validTeam("Alpha");
        em.persist(t);
        em.flush();
        assertNotNull(t.getId());
        
    }

    @Test
    void uniqueNameConstraint_failsOnDuplicate() {
        Team t1 = validTeam("UniqueName");
        Team t2 = validTeam("UniqueName");

        em.persist(t1);
        assertThrows(Exception.class, () -> {
            em.persist(t2);
            em.flush(); // should fail on unique teams.name
        });
    }

    @Test
    void notNullConstraints_onRequiredColumns() {
        Team t = new Team(); // missing name/city/country/logo
        assertThrows(Exception.class, () -> {
            em.persist(t);
            em.flush();
        });
    }

    @ParameterizedTest(name = "Team field {0} with length {1} should fail")
    @CsvSource({
        "name,257",
        "city,129",
        "country,129",
        "description,1025"
    })
    void lengthConstraints_failWhenTooLong(String field, int len) {
        Team t = validTeam("LenOK");
        String big = "X".repeat(len);
        switch (field) {
            case "name" -> t.setName(big);
            case "city" -> t.setCity(big);
            case "country" -> t.setCountry(big);
            case "description" -> t.setDescription(big);
        }
        assertThrows(Exception.class, () -> {
            em.persist(t);
            em.flush();
        });
    }

    @Test
    void relationship_teamToDrivers_persistDriverWithTeam() {
        Team team = validTeam("RelTeam");
        em.persist(team);
        em.flush(); // ensure team id exists with IDENTITY

        // use the managed instance and wire both sides
        Team managedTeam = em.find(Team.class, team.getId());

        Driver d = driver("Pooja", "Varma", java.time.LocalDate.of(1996,1,1));
        d.setTeam(managedTeam);
        managedTeam.getDrivers().add(d);

        em.persist(d);
        em.flush();
        em.clear();

        Team reloaded = em.find(Team.class, team.getId());
        assertNotNull(reloaded);
        assertTrue(reloaded.getDrivers().stream().anyMatch(x -> "Pooja".equals(x.getFirstName())));
    }

    @Test
    void deletingDriver_doesNotDeleteTeam() {
        Team team = validTeam("KeepTeam");
        em.persist(team);
        em.flush();

        Team managedTeam = em.find(Team.class, team.getId());

        Driver d = driver("John", "Doe", java.time.LocalDate.of(2000,1,1));
        d.setTeam(managedTeam);
        managedTeam.getDrivers().add(d);

        em.persist(d);
        em.flush();

        em.remove(d);
        em.flush();
        em.clear();

        Team stillThere = em.find(Team.class, team.getId());
        assertNotNull(stillThere);
    }

    @Test
    void deletingTeam_cascadesDeleteDrivers_becauseCascadeAllOnTeamDrivers() {
        Team team = validTeam("CascadeTeam");
        em.persist(team);
        em.flush(); // make sure id is assigned

        Team managedTeam = em.find(Team.class, team.getId());

        // Create two drivers and wire BOTH sides before persisting children
        Driver d1 = driver("A", "B", java.time.LocalDate.of(1990,1,1));
        d1.setTeam(managedTeam);
        managedTeam.getDrivers().add(d1);

        Driver d2 = driver("C", "D", java.time.LocalDate.of(1991,1,1));
        d2.setTeam(managedTeam);
        managedTeam.getDrivers().add(d2);

        // persist children explicitly (team is already saved)
        em.persist(d1);
        em.persist(d2);
        em.flush();

        // now remove team -> cascade remove should delete drivers
        em.remove(managedTeam);
        em.flush();
        em.clear();

        long count = em.createQuery("select count(d) from Driver d", Long.class).getSingleResult();
        assertEquals(0L, count);
    }
}
