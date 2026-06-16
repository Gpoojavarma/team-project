
// src/main/java/com/example/TeamAppDemo/TeamAppDemoApplication.java
package com.example.TeamAppDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")  // ensure all packages are scanned
public class TeamAppDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeamAppDemoApplication.class, args);
        System.out.println("✅ Application started at: http://localhost:8080");
        System.out.println("👉 Open H2 console at: http://localhost:8080/h2-console");
    }
}

