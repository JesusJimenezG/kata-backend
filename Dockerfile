FROM eclipse-temurin:21-jdk AS base
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

FROM base AS dev
RUN ./gradlew dependencies
COPY src src
EXPOSE 8080
CMD ["./gradlew", "bootRun", "--continuous"]

FROM base AS build
COPY src src
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre AS prod
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
