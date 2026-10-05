You are a senior Java/Spring Boot architect. Build a clean, industrial-standard Spring Boot ERP backend starter project whose primary purpose is to establish a reliable foundation and verify deployment to Render.

The project must be intentionally simple in business functionality, but the architecture, configuration, package structure, database access, API design, error handling, environment configuration, and deployment setup must be production-oriented.

## 1. Project identity

Project name:

`ncc-core-erp-backend`

Maven coordinates:

* Group ID: `com.nccaa`
* Artifact ID: `ncc-core-erp-backend`
* Version: `0.0.1-SNAPSHOT`

The project is an ERP-style REST API backend.

Technology stack:

* Java 17
* Spring Boot 4.1.1
* Maven
* Spring Web MVC / REST API
* Spring Data JPA
* Hibernate
* MySQL
* Lombok
* Spring Boot Actuator
* Bean Validation
* JUnit / Spring Boot testing
* Embedded Tomcat for Render deployment

Use a WAR-capable project if appropriate for Spring Boot, but make sure it can run directly with:

`mvn spring-boot:run`

and:

`java -jar target/ncc-core-erp-backend-0.0.1-SNAPSHOT.war`

The final application must work as a standalone Spring Boot application on Render.

Do NOT introduce unnecessary technologies such as Docker, Redis, Kafka, MongoDB, Kubernetes, GraphQL, or microservices at this stage.

---

# 2. Architecture

Use a clean layered architecture suitable for an ERP system.

Use a package structure similar to:

com.nccaa
├── NccCoreErpBackendApplication.java
│
├── config
│   └── ...
│
├── controller
│   └── HealthController.java
│
├── dto
│   └── ...
│
├── entity
│   └── ...
│
├── repository
│   └── ...
│
├── service
│   └── ...
│
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ...
│
├── mapper
│   └── ...
│
└── common
└── ...

Keep responsibilities separated:

Controller
→ HTTP/API layer only

Service
→ business/application logic

Repository
→ database access

Entity
→ JPA persistence model

DTO
→ API request/response model

Mapper
→ Entity ↔ DTO conversion

Exception
→ centralized API error handling

Config
→ application configuration

Do not put business logic directly inside controllers.

Do not expose JPA entities directly from REST endpoints unless there is a very strong reason.

---

# 3. Configuration structure

Create:

`src/main/resources/application.properties`

and environment-specific configuration:

`src/main/resources/application-dev.properties`

`src/main/resources/application-prod.properties`

The application must support environment variables.

Use environment variables for:

* database URL
* database username
* database password
* server port
* active Spring profile

Example conceptual configuration:

DB URL:
`${DB_URL:jdbc:mysql://localhost:3306/ncc_core_erp}`

DB username:
`${DB_USERNAME:root}`

DB password:
`${DB_PASSWORD:}`

Server port:
`${PORT:8080}`

Do not hard-code production credentials.

Do not commit passwords or secrets.

---

# 4. Local MySQL configuration

Assume MySQL is already installed locally.

The local database should be:

`ncc_core_erp`

Create clear instructions for:

```sql
CREATE DATABASE ncc_core_erp
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Local development should use:

```text
jdbc:mysql://localhost:3306/ncc_core_erp
```

Configure Hibernate/JPA appropriately.

For development:

```properties
spring.jpa.hibernate.ddl-auto=update
```

For production:

DO NOT blindly use:

```properties
spring.jpa.hibernate.ddl-auto=create
```

Prefer:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Explain that a proper migration tool such as Flyway should be introduced before the ERP reaches real production.

Do not add Flyway unless necessary for this initial deployment test.

---

# 5. Database design for the test

Create one simple but realistic ERP entity to verify the entire database stack.

Use:

`Department`

Fields:

* id
* code
* name
* description
* active
* createdAt
* updatedAt

Requirements:

* `id` should use a generated primary key.
* `code` must be unique.
* `name` must not be null.
* `active` should default to true.
* timestamps should be automatically maintained.
* Use appropriate JPA annotations.
* Add database indexes/constraints where appropriate.

Use:

```text
DepartmentEntity
DepartmentRepository
DepartmentService
DepartmentController
DepartmentRequest
DepartmentResponse
```

or an equivalent clean naming convention.

The purpose is not to build a complete ERP yet.

The purpose is to prove:

REST API
→ Controller
→ DTO
→ Service
→ Repository
→ JPA
→ MySQL

works correctly.

---

# 6. REST API

Create:

`/api/v1/departments`

Implement:

### GET

```http
GET /api/v1/departments
```

Return all departments.

### GET by ID

```http
GET /api/v1/departments/{id}
```

### POST

```http
POST /api/v1/departments
```

Example:

```json
{
  "code": "HR",
  "name": "Human Resources",
  "description": "Human resources department",
  "active": true
}
```

### PUT

```http
PUT /api/v1/departments/{id}
```

### DELETE

```http
DELETE /api/v1/departments/{id}
```

Use proper HTTP status codes.

Examples:

* 200 OK
* 201 CREATED
* 204 NO CONTENT
* 400 BAD REQUEST
* 404 NOT FOUND
* 409 CONFLICT
* 500 INTERNAL SERVER ERROR

Use `ResponseEntity` only where it improves clarity; do not wrap everything unnecessarily.

---

# 7. API response design

Create a consistent API response/error structure.

For errors, return something similar to:

```json
{
  "timestamp": "2026-10-05T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Department not found",
  "path": "/api/v1/departments/99"
}
```

Implement centralized exception handling using:

`@RestControllerAdvice`

Handle at minimum:

* Resource not found
* Validation errors
* Duplicate/constraint violations
* Illegal arguments
* Generic unexpected exceptions

Do not expose stack traces, SQL statements, database passwords, or internal implementation details in API responses.

---

# 8. Validation

Use Jakarta Bean Validation.

For example:

```text
code:
@NotBlank
@Size(...)

name:
@NotBlank
@Size(...)

description:
@Size(...)
```

Validation errors must produce clean JSON responses.

Do not perform all validation manually inside controllers.

---

# 9. Actuator / deployment health

Add Spring Boot Actuator.

Expose only useful endpoints such as:

```text
/actuator/health
```

The application must provide a simple health endpoint that Render can use.

Also create a simple application endpoint:

```text
GET /api/v1/health
```

Response:

```json
{
  "status": "UP",
  "application": "ncc-core-erp-backend"
}
```

Make sure the application starts successfully even before business data exists.

---

# 10. Render deployment requirements

Prepare the project specifically for Render.

The application must NOT assume:

```text
localhost
```

for production database connectivity.

Production database configuration must come from environment variables.

The application must listen on:

```properties
server.port=${PORT:8080}
```

Create a Render deployment guide explaining:

### Build command

```bash
./mvnw clean package -DskipTests
```

### Start command

Use the generated artifact appropriately, for example:

```bash
java -jar target/ncc-core-erp-backend-0.0.1-SNAPSHOT.war
```

If Spring Boot's executable WAR setup makes a JAR-style startup preferable, configure it correctly and document the exact command.

Do not guess. Verify that the generated artifact and command actually work.

---

# 11. Render environment variables

Document these variables:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=...
DB_USERNAME=...
DB_PASSWORD=...
PORT=10000
```

Do not hard-code Render credentials.

Do not commit `.env` files containing secrets.

Create:

`.env.example`

with placeholders only.

Example:

```text
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:mysql://localhost:3306/ncc_core_erp
DB_USERNAME=root
DB_PASSWORD=
```

---

# 12. Important Render/MySQL consideration

Clearly document that Render application hosting and the local MySQL server are not the same network.

The deployed Render application cannot connect to:

```text
localhost:3306
```

on the developer's personal computer.

Therefore:

* Local development → local MySQL.
* Render deployment → publicly/network-accessible production database.
* Use Render-compatible managed MySQL or another properly accessible MySQL-compatible database for production.

Do not attempt to solve this using hard-coded local IP addresses.

---

# 13. application.properties quality

Make the configuration clean and readable.

Use logical sections/comments, for example:

```properties
# =========================================================
# Application
# =========================================================

spring.application.name=ncc-core-erp-backend
server.port=${PORT:8080}

# =========================================================
# Database
# =========================================================

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/ncc_core_erp}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

# =========================================================
# JPA / Hibernate
# =========================================================

spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:update}
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=false

# =========================================================
# Actuator
# =========================================================

management.endpoints.web.exposure.include=health
```

Adjust properties if Spring Boot 4.1.1 requires a different configuration.

Do not blindly copy configuration that is incompatible with the selected Spring Boot version.

---

# 14. Logging

Configure sensible logging.

Development should allow useful SQL/application debugging.

Production should avoid noisy SQL logging and sensitive information.

Never log:

* passwords
* authentication tokens
* database credentials
* secrets
* sensitive personal information

Use SLF4J/standard Spring Boot logging.

Do not use `System.out.println()` for application logging.

---

# 15. Security preparation

Do NOT implement a full authentication system yet.

However, structure the project so Spring Security/JWT can be introduced later without restructuring the entire application.

Do not create fake authentication just for the demo.

Keep security-related configuration isolated so that the future architecture can support:

```text
Authentication
Authorization
Roles
Permissions
Access Scope
Audit Logging
```

This is an ERP foundation, so avoid architectural shortcuts that would make RBAC difficult later.

---

# 16. ERP scalability

The project will eventually contain modules such as:

```text
Administration
Users
Roles
Permissions
Cadets
Employees
Organizations
Departments
Locations
Districts
Provinces
Reports
Audit Logs
Notifications
File Management
```

Do NOT implement all of these now.

However, structure the project so they can be added without turning the application into a single huge controller/service/package.

Keep the initial Department module as the reference implementation for future modules.

---

# 17. Database naming

Use consistent naming conventions.

Prefer:

```text
snake_case
```

for database tables/columns.

Example:

```text
departments
created_at
updated_at
```

Java should use standard camelCase:

```text
createdAt
updatedAt
```

Do not hard-code database IDs in application logic.

---

# 18. Git structure

Create:

`.gitignore`

It must exclude at minimum:

```text
target/
.idea/
*.iml
.classpath
.project
.settings/
.env
```

Do not commit:

* passwords
* local configuration containing secrets
* generated build files
* IDE-specific files
* logs
* temporary files

---

# 19. README

Create a professional `README.md`.

It must include:

1. Project overview
2. Technology stack
3. Architecture
4. Requirements
5. Local MySQL setup
6. Environment variables
7. How to run locally
8. How to test
9. API endpoints
10. Render deployment
11. Production configuration
12. Future architecture/modules

Include exact commands.

Example:

```bash
./mvnw clean test
```

```bash
./mvnw spring-boot:run
```

```bash
./mvnw clean package
```

---

# 20. Testing

Create tests for the Department module.

At minimum:

### Repository

Test basic persistence.

### Service

Test:

* create
* retrieve
* update
* delete
* not-found behavior

### Controller

Test:

* GET
* POST
* PUT
* DELETE
* validation failure
* not found

Use appropriate Spring Boot testing facilities.

The project must pass:

```bash
./mvnw test
```

before considering the implementation complete.

---

# 21. Maven quality

Clean up the generated `pom.xml`.

Keep dependencies only when they are actually required.

Use Spring Boot's dependency management instead of unnecessarily specifying dependency versions manually.

Keep:

* Spring Data JPA
* Spring Web MVC
* MySQL
* Lombok
* Actuator
* Validation
* test dependencies

Remove unnecessary generated dependencies.

Do not add random libraries just because they are commonly used.

---

# 22. Code quality requirements

Follow these rules:

* No duplicated business logic.
* No giant controllers.
* No giant service classes.
* No static database configuration.
* No hard-coded credentials.
* No hard-coded environment-specific URLs.
* No SQL concatenation.
* Use parameterized repository queries.
* Use meaningful class and method names.
* Use constructor injection.
* Prefer immutable DTOs where practical.
* Keep methods small and focused.
* Use appropriate Java 17 features.
* Keep API contracts separate from persistence entities.
* Avoid premature abstractions.
* Avoid unnecessary interfaces when there is no architectural benefit.
* Add comments only where they explain non-obvious decisions.

---

# 23. Final verification

After creating the project, do a complete verification.

Run:

```bash
./mvnw clean test
```

Then:

```bash
./mvnw clean package
```

Then start the application locally.

Verify:

```text
GET /api/v1/health
GET /actuator/health
GET /api/v1/departments
POST /api/v1/departments
GET /api/v1/departments/{id}
PUT /api/v1/departments/{id}
DELETE /api/v1/departments/{id}
```

Verify that data is actually persisted in local MySQL.

Verify that invalid requests return proper validation errors.

Verify that nonexistent IDs return 404.

Verify that duplicate department codes are handled cleanly.

Verify that the production configuration does not contain local credentials or hard-coded secrets.

Verify that the packaged WAR can start successfully.

---

# 24. Deliverables

At the end, provide:

1. Complete project structure.
2. Complete `pom.xml`.
3. All Java source files.
4. `application.properties`.
5. `application-dev.properties`.
6. `application-prod.properties`.
7. `.env.example`.
8. `.gitignore`.
9. `README.md`.
10. Tests.
11. Render deployment instructions.
12. Exact commands used for verification.
13. A short deployment checklist.

Most importantly, DO NOT merely generate files without testing them.

Build the project, run the tests, package it, start it locally, verify the REST API and MySQL connection, and fix any errors you encounter.

The final result should be a clean foundation for the future NCCAA ERP—not a throwaway demo.
