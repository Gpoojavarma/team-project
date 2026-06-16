
# TeamAppDemo

Spring Boot backend for Teams, Drivers, and Races with validations, business rules, and Swagger UI.

## Run

```bash
cd TeamAppDemo/TeamAppDemo
mvn spring-boot:run
```

Open Swagger UI: <http://localhost:8080/swagger-ui.html>

## API Summary
- `POST /api/teams` (multipart: `request` JSON + `logo` file ≤50KB)
- `PUT /api/teams/{id}`
- `PUT /api/teams/{id}/logo` (multipart)
- `GET /api/teams/{id}` / `GET /api/teams`
- `DELETE /api/teams/{id}` (blocked if any team driver is registered to ≥1 race)

- `POST /api/drivers`
- `PUT /api/drivers/{id}`
- `GET /api/drivers/{id}` / `GET /api/drivers`
- `DELETE /api/drivers/{id}` (blocked if registered to ≥1 race)
- `PUT /api/drivers/{id}/team/{teamId}`

- `POST /api/races`
- `PUT /api/races/{id}`
- `GET /api/races/{id}` / `GET /api/races`
- `DELETE /api/races/{id}` (blocked if any driver registered)
- `POST /api/races/{raceId}/registrations/{driverId}`
- `DELETE /api/races/{raceId}/registrations/{driverId}`

## Validation Rules
- Team: name unique (≤256), location mandatory, logo mandatory and ≤50KB, description ≤1024.
- Driver: first/last name mandatory (≤96), DoB not later than 2000-12-31.
- Race: track name unique (≤256), location mandatory, raceDate future, registrationClosureDate optional past.

## Notes
- Uses H2 in-memory DB.
- Static pages under `/` just show JSON lists.
