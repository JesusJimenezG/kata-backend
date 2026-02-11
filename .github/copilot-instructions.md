# Copilot instructions for kata-backend

## Rule loading (required)

This repo keeps detailed, task-specific rules in [.github/instructions](.github/instructions). When working as an agent, **read and follow these files**:

1. Always apply: [.github/instructions/GENERAL.md](.github/instructions/GENERAL.md) and [.github/instructions/AI_USAGE.md](.github/instructions/AI_USAGE.md)
2. When making code changes that affect repo history (almost always): [.github/instructions/GIT.md](.github/instructions/GIT.md)
3. When implementing product behavior/endpoints for the “Resource Management and Reservation Platform”: [.github/instructions/FUNCTIONAL_REQUIREMENTS.md](.github/instructions/FUNCTIONAL_REQUIREMENTS.md)

If rules conflict, use this precedence: FUNCTIONAL_REQUIREMENTS (product semantics) > GENERAL (tech constraints) > GIT (change hygiene).

## Project snapshot (for quick orientation)

- Spring Boot 4.0.2, Java 21, Gradle; entry point: [src/main/java/dev/jesusjimenezg/kata/KataApplication.java](src/main/java/dev/jesusjimenezg/kata/KataApplication.java)
- Persistence: Spring Data JPA (Hibernate)
- Schema migrations: Flyway (SQL scripts in `src/main/resources/db/migration/`)
- Runtime DB: Postgres via [compose.yaml](compose.yaml)
- Config: [src/main/resources/application.yaml](src/main/resources/application.yaml)

## Commands

- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Tests: `./gradlew test`
