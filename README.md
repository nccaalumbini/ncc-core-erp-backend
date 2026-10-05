# NCCAA Core ERP Backend

Foundational REST API backend for the NCCAA ERP, built with Spring Boot. The business
scope is intentionally small (a single **Department** module) so the project can prove the
full stack — REST → controller → DTO → service → repository → JPA → MySQL — and verify a
clean deployment to [Render](https://render.com), while remaining a solid architectural
base for the modules that will follow.

---

## 1. Project overview

* Production-oriented layered architecture and package structure.
* One reference ERP module (`Department`) as the pattern for future modules.
* Consistent JSON error contract with centralised exception handling.
* Jakarta Bean Validation on all request payloads.
* Environment-variable driven configuration (no hard-coded credentials).
* Spring Boot Actuator health endpoint plus an application health endpoint.
* Packaged as both a runnable Spring Boot application and a deployable WAR.
* Full test coverage for the repository, service, and controller layers.

---

## 2. Technology stack

| Concern            | Choice                                   |
|--------------------|------------------------------------------|
| Language           | Java 17                                  |
| Framework          | Spring Boot 4.1.1                        |
| Build              | Maven (with Maven Wrapper `mvnw`)        |
| Web                | Spring Web MVC (REST)                    |
| Persistence        | Spring Data JPA / Hibernate              |
| Database           | MySQL 8+ (MariaDB-compatible)            |
| Validation         | Jakarta Bean Validation                  |
| Boilerplate        | Lombok                                   |
| Ops / health       | Spring Boot Actuator                     |
| Testing            | JUnit 5, Mockito, MockMvc, H2 (in tests) |
| Runtime            | Embedded Tomcat                          |

> **Spring Boot 4 / Jackson 3 note:** Spring Boot 4 serialises `java.time` values as
> ISO-8601 strings by default and uses `tools.jackson.databind` (Jackson 3) on the
> classpath. Configuration and tests reflect this — for example,
> `spring.jackson.serialization.write-dates-as-timestamps` no longer exists.

---

## 3. Architecture

Layered, with clear responsibilities:

```text
Controller  -> HTTP/API layer only (no business logic)
Service     -> business/application logic, transactions
Repository  -> database access (Spring Data JPA)
Entity      -> JPA persistence model (never exposed from REST)
DTO         -> API request/response models
Mapper      -> Entity <-> DTO conversion
Exception   -> centralised API error handling
Config      -> cross-cutting configuration (future security/RBAC lives here)
```

`GlobalExceptionHandler` (`@RestControllerAdvice`) translates exceptions into consistent
JSON. See section 7 for the exact error shape.

---

## 4. Requirements

* Java 17 or newer (`java -version`) — the Maven Wrapper and build target Java 17.
* MySQL 8+ (or MariaDB) available locally for development.
* No separate Maven install required; use `./mvnw` (Maven Wrapper).
* Network access to Maven Central for the first build.

---

## 5. Local MySQL setup

Create the development database:

```sql
CREATE DATABASE ncc_core_erp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Local connection URL:

```text
jdbc:mysql://localhost:3306/ncc_core_erp
```

For development, Hibernate manages the schema:

```properties
spring.jpa.hibernate.ddl-auto=update
```

For production, the schema must **not** be created/altered automatically:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

> **Migration tooling:** before this ERP reaches real production, introduce a proper
> migration tool such as **Flyway** or **Liquibase**. It is deliberately not added at this
> stage so the initial deployment path stays minimal.

---

## 6. Environment variables

| Variable                 | Purpose                                  | Local default                          |
|--------------------------|------------------------------------------|----------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Active profile (`dev` / `prod`)          | `dev`                                  |
| `PORT`                   | HTTP port to bind                        | `8080`                                 |
| `DB_URL`                 | JDBC URL                                 | `jdbc:mysql://localhost:3306/ncc_core_erp` |
| `DB_USERNAME`            | Database user                            | `root`                                 |
| `DB_PASSWORD`            | Database password                        | *(empty)*                              |
| `JPA_DDL_AUTO`           | Optional override of `ddl-auto`          | `update` (dev), `validate` (prod)      |

Copy `.env.example` to `.env` for local use. **Never commit real credentials.** `.env` is
excluded by `.gitignore`.

### How `.env` is loaded (important)

Spring Boot does **not** read `.env` files by default. This project explicitly imports it in
`application.properties`:

```properties
spring.config.import=optional:file:.env[.properties]
```

* The import is `optional`, so a missing `.env` is simply skipped (this is the Render case —
  there, real environment variables are used).
* Real OS environment variables **override** values from `.env`.
* Run the app from the project root (where `.env` lives), e.g. `./mvnw spring-boot:run` or
  `java -jar target/ncc-core-erp-backend-0.0.1-SNAPSHOT.war`.

So for local development you can either put values in `.env`:

```text
DB_USERNAME=root
DB_PASSWORD=your-local-password
```

…or export them in your shell / IDE run configuration. You do not need to edit
`application.properties` itself.


---

## 7. API endpoints

Base path: `/api/v1`.

| Method   | Path                       | Success        | Description                  |
|----------|----------------------------|----------------|------------------------------|
| `GET`    | `/`                        | `200`          | Service identification       |
| `GET`    | `/api/v1/health`           | `200`          | Application health           |
| `GET`    | `/actuator/health`         | `200`          | Actuator health (Render)     |
| `GET`    | `/api/v1/departments`      | `200`          | List departments (by name)   |
| `GET`    | `/api/v1/departments/{id}` | `200` / `404`  | Get one department           |
| `POST`   | `/api/v1/departments`      | `201` / `400` / `409` | Create a department   |
| `PUT`    | `/api/v1/departments/{id}` | `200` / `400` / `404` / `409` | Update a department |
| `DELETE` | `/api/v1/departments/{id}` | `204` / `404`  | Delete a department          |

**Create example**

```http
POST /api/v1/departments
Content-Type: application/json

{
  "code": "HR",
  "name": "Human Resources",
  "description": "Human resources department",
  "active": true
}
```

**Error shape**

```json
{
  "timestamp": "2026-10-05T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Department not found with id: 99",
  "path": "/api/v1/departments/99"
}
```

Validation failures add a `validationErrors` array (`field` + `message`). Stack traces, SQL
statements, credentials, and other internals are never returned.

---

## 8. How to run locally

```bash
# 1. Create the database (see section 5), then:
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` with the `dev` profile by default.

Run the packaged artifact:

```bash
./mvnw clean package
java -jar target/ncc-core-erp-backend-0.0.1-SNAPSHOT.war
```

Because the WAR embeds Tomcat at a *provided* scope, the executable WAR both runs
standalone (`java -jar ...`) and can be deployed to an external servlet container.

Quick checks:

```bash
curl http://localhost:8080/
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/departments
```

---

## 9. How to test

```bash
./mvnw clean test
```

Tests use an in-memory H2 database in MySQL-compatibility mode
(`src/test/resources/application.properties`) and never touch a real MySQL instance.

Coverage:

* `DepartmentRepositoryTest` — persistence, generated keys, timestamps, unique/code lookups.
* `DepartmentServiceTest` — create, retrieve, update, delete, duplicate handling, not-found.
* `DepartmentControllerTest` — GET/POST/PUT/DELETE, validation failure, 404s, 201 + Location,
  unexpected 500 without leaking details.
* `RootControllerTest` — `GET /` identification payload.
* `HttpEndpointsIntegrationTest` — `GET /`, `/api/v1/health`, `/actuator/health`, unknown
  paths (clean 404), `/favicon.ico`.
* `NccCoreErpBackendApplicationTests` — full application context startup.

---

## 10. Render deployment

### Why Docker

Render has **no native Java/JVM runtime** — its native runtimes are JavaScript/TypeScript,
Python, Ruby, Go, Rust, and Elixir. Render's own documentation says to use **Docker** for
JVM-based languages. This project therefore ships a `Dockerfile` (multi-stage: Maven build
with JDK 17, then a slim JRE 17 runtime) and a `render.yaml` Blueprint.

### Option A — Blueprint (recommended)

1. Push this repo to Bitbucket/GitHub.
2. In Render, choose **New → Blueprint** and point it at the repo. `render.yaml` creates the
   web service with `runtime: docker`, `healthCheckPath: /actuator/health`, and
   `SPRING_PROFILES_ACTIVE=prod`.
3. Set the `sync: false` env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) in the dashboard.

### Option B — Manual Docker web service

| Setting            | Value                                             |
|--------------------|---------------------------------------------------|
| Runtime            | Docker                                            |
| Dockerfile path    | `./Dockerfile`                                    |
| Health check path  | `/actuator/health`                                |
| Start command      | *(leave empty — the image `ENTRYPOINT` starts it)* |

The image runs `java -jar /app/app.war`; no `mvnw` command is needed on Render.

### Equivalent build/run locally (without Docker)

```bash
./mvnw clean package -DskipTests
java -jar target/ncc-core-erp-backend-0.0.1-SNAPSHOT.war
```

### Environment variables (Render dashboard)

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<managed-mysql-host>:<port>/<database>?sslMode=REQUIRED
DB_USERNAME=<user>
DB_PASSWORD=<password>
```

`PORT` is injected by Render automatically; the app binds to `${PORT:8080}`.

### Health check path (Render)

Use **`/actuator/health`** as the Render health check path. Do not point Render at `/`;
the root endpoint is informational only.

```text
Health Check Path: /actuator/health
```

### Note on the `RESERVED` metadata warning

Startup may log:

```text
Could not obtain connection metadata: java.sql.SQLSyntaxErrorException: Unknown column 'RESERVED' in 'WHERE'
```

This comes from MySQL Connector/J (9.x) issuing
`SELECT WORD FROM INFORMATION_SCHEMA.KEYWORDS WHERE RESERVED = 1`. On **MySQL 8+** that
column exists and the query succeeds. It only appears against **MariaDB 11+**, whose
`INFORMATION_SCHEMA.KEYWORDS` no longer exposes `RESERVED`. It is a Connector/J ↔ MariaDB
metadata incompatibility, not a Hibernate or application defect. On MySQL 8 (the target
database, including Render's managed MySQL) it does not occur. If you must run against a
modern MariaDB, add `useInformationSchema=false` to `DB_URL`; that switches metadata
introspection to the legacy `SHOW` statements and removes the warning.

### Important: Render app hosting ≠ local MySQL

Render application hosting and your local MySQL server are **not on the same network**. A
deployed Render instance cannot reach `localhost:3306` on your machine. Therefore:

* **Local development** → local MySQL.
* **Render deployment** → a network-accessible managed MySQL (e.g. a Render-managed
  MySQL-compatible database, or another properly exposed provider).

Never hard-code local IP addresses to bridge this.

---

## 11. Production configuration

Production settings live in `application-prod.properties` and assume **all** connectivity
comes from environment variables:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:validate}
spring.jpa.show-sql=false
```

No SQL/parameter logging, no credentials, no local URLs. Actuator exposes only `health`.

---

## 12. Future architecture / modules

The Department module is the reference pattern. Planned modules include:

```text
Administration   Users        Roles        Permissions
Cadets           Employees    Organizations Departments
Locations        Districts    Provinces    Reports
Audit Logs       Notifications             File Management
```

Security is intentionally **not** implemented yet. `com.nccaa.config` is reserved for the
future Spring Security / JWT configuration so that authentication, authorization, roles,
permissions, access scope, and audit logging can be layered in without restructuring the
domain modules. Do not add fake authentication in the meantime.

---

## 13. Deployment checklist

- [ ] `./mvnw clean test` passes locally.
- [ ] `./mvnw clean package` produces `target/ncc-core-erp-backend-0.0.1-SNAPSHOT.war`.
- [ ] `java -jar target/...war` starts and serves `/actuator/health` with `UP`.
- [ ] Render service uses the **Docker** runtime with `Dockerfile` at repo root (`render.yaml` does this).
- [ ] `SPRING_PROFILES_ACTIVE=prod` and all `DB_*` variables set in the Render dashboard.
- [ ] Production database is network-accessible from Render (not `localhost`).
- [ ] No secrets committed (`.env` ignored; no credentials in properties/Dockerfile).
- [ ] A migration tool (Flyway/Liquibase) is scheduled before real production data.

---

## 14. Project structure

```text
src/main/java/com/nccaa
├── NccCoreErpBackendApplication.java
├── ServletInitializer.java
├── common/       HealthStatus.java, ApplicationInfo.java
├── config/       (future cross-cutting config: security, RBAC)
├── controller/   RootController.java, HealthController.java, DepartmentController.java
├── dto/          DepartmentRequest.java, DepartmentResponse.java, ApiError.java
├── entity/       DepartmentEntity.java
├── exception/    GlobalExceptionHandler.java, ResourceNotFoundException.java,
│                 DuplicateResourceException.java
├── mapper/       DepartmentMapper.java
├── repository/   DepartmentRepository.java
└── service/      DepartmentService.java

Dockerfile            Multi-stage image used by Render
render.yaml           Render Blueprint (Docker service + health check)
.dockerignore         Keeps build context lean
```
