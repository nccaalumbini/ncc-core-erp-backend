# =========================================================
# Multi-stage build for the NCCAA Core ERP backend
# =========================================================
# Render has no native JVM runtime, so the application is built and run inside
# this image. Stage 1 compiles with Maven + JDK 17; stage 2 runs on a slim JRE.

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies separately from source for faster rebuilds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as a non-root user.
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/target/*.war /app/app.war
RUN chown app:app /app/app.war
USER app

# Render injects PORT at runtime; the app binds to ${PORT:8080}.
EXPOSE 8080

# The WAR is a Spring Boot executable archive (embedded Tomcat is bundled in
# WEB-INF/lib-provided), so it starts exactly like a JAR.
ENTRYPOINT ["java", "-jar", "/app/app.war"]
