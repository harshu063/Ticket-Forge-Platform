# Ticket Forge Platform

![Build](https://img.shields.io/badge/build-compile%20verified-brightgreen)
![Java](https://img.shields.io/badge/java-21-blue)
![License](https://img.shields.io/badge/license-not%20specified-lightgrey)
![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.3-6DB33F)
![Docker](https://img.shields.io/badge/docker-compose-2496ED)

Secure event-ticketing backend built with Spring Boot, PostgreSQL, Keycloak JWT auth, and QR-based ticket workflows.

## Quickstart

```powershell
docker compose up -d db adminer keycloak
.\mvnw.cmd spring-boot:run
```

- API: `http://localhost:8081`
- Adminer: `http://localhost:8888`
- Keycloak: `http://localhost:9090`

---

## Current implementation status

Implemented and working in current design:

- JWT authentication and authorization (OAuth2 Resource Server + Keycloak)
- Validation APIs and centralized exception handling
- Event, ticket, purchase, QR retrieval, and ticket validation API layers
- QR code generation service flow (`QrCodeService`)
- Concurrency-aware purchase logic (`@Transactional` availability check + sold-out protection)
- 5 controllers and 5 services wired in the application architecture

---

## What this project does

This backend supports an event-ticketing workflow where authenticated users can:

- Create and manage organizer events
- Purchase tickets for ticket types
- Retrieve ticket details and QR images
- Validate tickets via QR scan or manual mode

The domain includes entities for `Event`, `TicketType`, `Ticket`, `QrCode`, `TicketValidation`, and `User`.

---

## Tech stack

- Java 21
- Spring Boot 4 (Web MVC, Security, Data JPA, Validation)
- Spring Security OAuth2 Resource Server (JWT)
- PostgreSQL + Hibernate
- MapStruct + Lombok
- Keycloak
- Docker Compose + Adminer
- ZXing (QR generation)

---

## Architecture snapshot

### Controllers (5)

- `src/main/java/com/example/ticket/controllers/EventController.java`
- `src/main/java/com/example/ticket/controllers/TicketController.java`
- `src/main/java/com/example/ticket/controllers/TicketTypeController.java`
- `src/main/java/com/example/ticket/controllers/TicketValidationController.java`
- `src/main/java/com/example/ticket/controllers/GlobalExceptionHandler.java`

### Services (5)

- `src/main/java/com/example/ticket/services/EventService.java`
- `src/main/java/com/example/ticket/services/TicketService.java`
- `src/main/java/com/example/ticket/services/TicketTypeService.java`
- `src/main/java/com/example/ticket/services/QrCodeService.java`
- `src/main/java/com/example/ticket/services/TicketValidationService.java`

Service implementations:

- `src/main/java/com/example/ticket/services/impl/EventServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/TicketServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/TicketTypeServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/QrCodeServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/TicketValidationServiceImpl.java`

---

## Local setup

### Prerequisites

- JDK 21
- Docker Desktop
- Git
- REST client (Postman, Bruno, Insomnia, or curl)

### Local service endpoints

- App: `http://localhost:8081`
- PostgreSQL host port: `5433`
- Adminer: `http://localhost:8888`
- Keycloak: `http://localhost:9090`
- OIDC discovery: `http://localhost:9090/realms/event-ticket-platform/.well-known/openid-configuration`

### Adminer login

- System: `PostgreSQL`
- Server: `db`
- Username: `postgres`
- Password: `Mnbvcxz@123`
- Database: `postgres`

---

## Authentication and security

- All APIs are protected by Spring Security.
- JWT issuer configured in `src/main/resources/application.properties`:
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/event-ticket-platform`
- Stateless API security (`SessionCreationPolicy.STATELESS`, CSRF disabled).

### User provisioning

`UserProvisioningFilter` creates local users from JWT claims:

- `sub` -> UUID
- `preferred_username` -> name
- `email` -> email

---

## Validation and error handling

Request DTOs use Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@NotEmpty`, `@PositiveOrZero`).

`GlobalExceptionHandler` handles:

- `UserNotFoundException` -> 404
- `MethodArgumentNotValidException` -> 400
- `ConstraintViolationException` -> 400
- fallback `Exception` -> 500

---

## API overview

### Event APIs

- `POST /api/v1/events`
- `PUT /api/v1/events/{eventId}`
- `GET /api/v1/events`
- `GET /api/v1/events/{eventId}`
- `DELETE /api/v1/events/{eventId}`

### Ticket purchase API

- `POST /api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets`

Implemented with transactional stock check:

- Counts sold tickets by ticket type
- Compares with `totalAvailable`
- Throws sold-out exception when exhausted

### Ticket APIs

- `GET /api/v1/tickets`
- `GET /api/v1/tickets/{ticketId}`
- `GET /api/v1/tickets/{ticketId}/qr-codes`

### Ticket validation API

- `POST /api/v1/ticket-validations`
  - `QR_SCAN`
  - `MANUAL`

---

## QR code support

Implemented:

- QR generation via `QrCodeServiceImpl.generateQrCode(...)`
- QR retrieval endpoint `GET /api/v1/tickets/{ticketId}/qr-codes`
- Validation endpoint `POST /api/v1/ticket-validations`

Domain enums/statuses include:

- `QrCodeStatusEnum`: `ACTIVE`, `EXPIRED`
- `TicketValidationMethod`: `QR_SCAN`, `MANUAL`
- `TicketValidationStatusEnum`: `VALID`, `INVALID`, `EXPIRED`

---

## Build and test

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test
```

---

## Concurrency implementation note

Concurrency-aware purchase behavior is implemented at service level using transactional execution and availability checks.

Next enhancement (scaling phase):

- Lock-based protection for very high contention (row locking or optimistic versioning)
- Reservation windows and retry-safe purchase/idempotency flows

---

## Troubleshooting

### Keycloak issuer errors / JWT decoder timeout

```powershell
docker compose up -d keycloak
```

### PostgreSQL connection issues

```powershell
docker compose up -d db
```

### Timezone alias issue (`Asia/Calcutta`)

The app sets JVM timezone to `Asia/Kolkata` at startup to avoid PostgreSQL timezone alias failures.

### Large inventory values

`totalAvailable` is modeled as `Long`. If an old schema still uses integer type, migrate to `BIGINT`.
