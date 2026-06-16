
package com.example.TeamAppDemo.DTO;

import java.time.LocalDate;
import java.util.List;

import com.example.TeamAppDemo.Validation.NotAfter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DriverDtos {
    public static class DriverCreateRequest {
        @NotBlank @Size(max=96) public String firstName;
        @NotBlank @Size(max=96) public String lastName;

        @NotNull @NotAfter("2000-12-31")
        public LocalDate dateOfBirth;

        public Long teamId;
    }

    public static class DriverUpdateRequest {
        @NotBlank @Size(max=96) public String firstName;
        @NotBlank @Size(max=96) public String lastName;
        @NotNull @NotAfter("2000-12-31") public LocalDate dateOfBirth;
        public Long teamId;
    }

    
public static class DriverResponse {
    public Long id;
    public String firstName;
    public String lastName;
    public LocalDate dateOfBirth;
    public Long teamId;
    private long registrationCount;
    public List<Long> raceIds;

    public long getRegistrationCount() {            // ✅ Jackson sees this
        return registrationCount;
    }
    public void setRegistrationCount(long registrationCount) {
        this.registrationCount = registrationCount;
    }
}

}
