
package com.example.TeamAppDemo.DTO;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class RaceDtos {
    public static class RaceCreateRequest {
        @NotBlank @Size(max=256) public String trackName;
        @NotBlank @Size(max=128) public String city;
        @NotBlank @Size(max=128) public String country;

        @NotNull @Future public LocalDate raceDate;
       
         public LocalDate registrationClosureDate;
    }

    public static class RaceUpdateRequest {
        @NotBlank @Size(max=256) public String trackName;
        @NotBlank @Size(max=128) public String city;
        @NotBlank @Size(max=128) public String country;

        @NotNull @Future public LocalDate raceDate;
       
       
        public LocalDate registrationClosureDate;

    }

    public static class RaceResponse {
        public Long id;
        public String trackName;
        public String city;
        public String country;
        public LocalDate raceDate;
        public LocalDate registrationClosureDate;
        public List<Long> registeredDriverIds;
    }
}
