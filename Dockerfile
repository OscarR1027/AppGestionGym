FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY demoappgym/pom.xml ./pom.xml
COPY demoappgym/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN groupadd --system app && useradd --system --uid 10001 --gid app app
COPY --from=build --chown=app:app /workspace/target/*.jar /app/app.jar
USER app

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
