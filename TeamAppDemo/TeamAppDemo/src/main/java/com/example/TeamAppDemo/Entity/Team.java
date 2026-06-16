
package com.example.TeamAppDemo.Entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "teams",
       uniqueConstraints = @UniqueConstraint(name = "uk_team_name", columnNames = "name"))
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable=false, length=256)
    private String name;

    @Column(name="city", nullable=false, length=128)
    private String city;

    



    @Column(name="country", nullable=false, length=128)
    private String country;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name="logo", nullable=false)
    private byte[] logo;

    @Column(name="description", length=1024)
    private String description;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Driver> drivers = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public byte[] getLogo() { return logo; }
    public String getDescription() { return description; }
    public List<Driver> getDrivers() { return drivers; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setLogo(byte[] logo) { this.logo = logo; }
    public void setDescription(String description) { this.description = description; }

}
