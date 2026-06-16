
package com.example.TeamAppDemo.Config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Race Demo API",
        version = "v1",
        description = "Teams, Drivers, Races CRUD with constraints"
    )
)
public class OpenApiConfig {
    // No @Bean and no direct reference to io.swagger.v3.oas.models.OpenAPI
}

