# General development rules for AI agents

- This is a Spring Boot 4.0.2 app using Java 21 and Gradle. The entry point is `KataApplication` in [src/main/java/dev/jesusjimenezg/kata/KataApplication.java](src/main/java/dev/jesusjimenezg/kata/KataApplication.java).
- Keep new code under the `dev.jesusjimenezg.kata` package unless a new package structure is explicitly requested.
- Persistence is JDBC-focused (Spring Data JDBC + JDBC starters). Do not add JPA/Hibernate unless explicitly requested.
- Configuration is YAML; base config is in [src/main/resources/application.yaml](src/main/resources/application.yaml).

## Local workflow (Gradle)

- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Tests: `./gradlew test` (JUnit 5; context smoke test in [src/test/java/dev/jesusjimenezg/kata/KataApplicationTests.java](src/test/java/dev/jesusjimenezg/kata/KataApplicationTests.java))

## Integration points

- PostgreSQL is the runtime database; local dev via [compose.yaml](compose.yaml) (`postgres` service, `myuser`/`secret`, db `mydatabase`).
- Spring Session JDBC is included; expect session tables to be managed via JDBC when enabled.
- Spring Security is on the classpath; assume endpoints may need security configuration when added.
