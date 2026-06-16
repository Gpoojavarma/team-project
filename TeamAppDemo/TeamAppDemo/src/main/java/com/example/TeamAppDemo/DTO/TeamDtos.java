
package com.example.TeamAppDemo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeamDtos {
    public static class TeamCreateRequest {
        @Schema(example = "Red Rockets")
        @NotBlank @Size(max=256)
        public String teamName;

        @NotBlank @Size(max=128)
        public String city;

        @NotBlank @Size(max=128)
        public String country;

        @Size(max=1024)
        public String description;
    }

    public static class TeamUpdateRequest {
        @Schema(example = "Red Rockets")
        @NotBlank @Size(max=256)
        public String teamName;

        @NotBlank @Size(max=128)
        public String city;

        @NotBlank @Size(max=128)
        public String country;

        @Size(max=1024)
        public String description;
    }

    public static class TeamResponse {
        public Long id;
        public String teamName;
        public String city;
        public String country;
        public String description;
        public boolean logoPresent;
         public String logoUrl;
         
        public List<Long> driverIds;
    }
}
