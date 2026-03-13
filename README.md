# Ticket Forge Platform

A Spring Boot backend for managing events, ticket types, and the foundations of a secure ticketing platform.

It currently supports authenticated event management for organizers, persists data in PostgreSQL, provisions users from JWT claims, and models the core entities needed for ticket purchase, QR code issuance, and ticket validation.

> **Current status**
> - **Implemented today:** authenticated event creation + organizer event listing, nested ticket-type creation, validation, persistence, Keycloak-backed JWT auth, automatic user provisioning.
> - **Modeled but not fully exposed yet:** ticket purchase flows, QR code generation, QR code validation, public event discovery, update/delete endpoints, and concurrency-safe ticket buying.

---

## Table of Contents

- [What this project does](#what-this-project-does)
- [Features](#features)
- [Architecture at a glance](#architecture-at-a-glance)
- [Tech stack](#tech-stack)
- [Repository links](#repository-links)
- [Local services and links](#local-services-and-links)
- [Prerequisites](#prerequisites)
- [How to run locally](#how-to-run-locally)
- [Authentication and security](#authentication-and-security)
- [Validation and error handling](#validation-and-error-handling)
- [API overview](#api-overview)
- [QR code and ticket validation support](#qr-code-and-ticket-validation-support)
- [Observability and metrics](#observability-and-metrics)
- [Recommended tools](#recommended-tools)
- [Testing](#testing)
- [Known limitations](#known-limitations)
- [Roadmap](#roadmap)
- [Troubleshooting](#troubleshooting)

---

## What this project does

This service is the backend of an event-ticketing platform.

Today, an authenticated organizer can:
- create an event,
- create ticket types as part of the event payload,
- list their own events,
- get automatically synced into the local `users` table from JWT claims.

The domain model is already prepared for:
- ticket ownership,
- QR code lifecycle,
- ticket validation,
- event staff/attendees,
- future concurrency-safe ticket buying.

---

## Features

### Implemented

- **JWT-protected API** using Spring Security OAuth2 Resource Server
- **Keycloak integration** via issuer URI validation
- **Automatic local user provisioning** from JWT claims (`sub`, `preferred_username`, `email`)
- **Create event API** with nested ticket types
- **List organizer events API** with pagination support
- **Bean validation** on request DTOs
- **Centralized exception handling** for validation and generic server errors
- **PostgreSQL persistence** with Spring Data JPA + Hibernate
- **MapStruct mapping layer** between DTOs and domain objects
- **Adminer support** for quick DB inspection
- **Timezone hardening** for PostgreSQL startup on Windows/JDK environments

### Domain support already modeled

- `Ticket`
- `TicketType`
- `QrCode`
- `TicketValidation`
- user attendance/staffing relationships
- event status and QR/validation status enums

### Planned / next steps

- ticket purchase flow
- inventory decrement logic
- QR code generation API
- QR scan / manual validation API
- published event discovery/search endpoints
- update/delete event flows
- concurrency-safe ticket buying
- proper operational metrics via Actuator/Micrometer

---

## Architecture at a glance

**Request flow**

1. Client sends Bearer token
2. Spring Security validates JWT using the configured issuer
3. `UserProvisioningFilter` ensures the user exists locally
4. Controller validates payload
5. MapStruct maps DTO -> domain request
6. Service layer applies business logic
7. JPA/Hibernate persists to PostgreSQL
8. Response DTO is returned

**Main layers**

- **Controller:** API endpoints
- **DTOs:** request/response contracts + validation
- **Mapper:** DTO/domain translation
- **Service:** business logic
- **Repository:** persistence abstraction
- **Entity:** relational model

---

## Tech stack

- [Java 21](https://www.oracle.com/java/technologies/downloads/)
- [Spring Boot 4](https://docs.spring.io/spring-boot/)
- [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Hibernate ORM](https://hibernate.org/orm/)
- [PostgreSQL](https://www.postgresql.org/)
- [MapStruct](https://mapstruct.org/)
- [Lombok](https://projectlombok.org/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Keycloak](https://www.keycloak.org/)
- [Adminer](https://www.adminer.org/)

---

## Repository links

### Core app
- [`src/main/java/com/example/ticket/TicketApplication.java`](src/main/java/com/example/ticket/TicketApplication.java)
- [`pom.xml`](pom.xml)
- [`docker-compose.yml`](docker-compose.yml)
- [`src/main/resources/application.properties`](src/main/resources/application.properties)
- [`HELP.md`](HELP.md)

### Security
- [`SecurityConfig`](src/main/java/com/example/ticket/config/SecurityConfig.java)
- [`UserProvisioningFilter`](src/main/java/com/example/ticket/filters/UserProvisioningFilter.java)

### API
- [`EventController`](src/main/java/com/example/ticket/controllers/EventController.java)
- [`GlobalExceptionHandler`](src/main/java/com/example/ticket/controllers/GlobalExceptionHandler.java)

### Service layer
- [`EventService`](src/main/java/com/example/ticket/services/EventService.java)
- [`EventServiceImpl`](src/main/java/com/example/ticket/services/impl/EventServiceImpl.java)

### Domain model
- [`Event`](src/main/java/com/example/ticket/domain/entities/Event.java)
- [`TicketType`](src/main/java/com/example/ticket/domain/entities/TicketType.java)
- [`Ticket`](src/main/java/com/example/ticket/domain/entities/Ticket.java)
- [`QrCode`](src/main/java/com/example/ticket/domain/entities/QrCode.java)
- [`TicketValidation`](src/main/java/com/example/ticket/domain/entities/TicketValidation.java)
- [`User`](src/main/java/com/example/ticket/domain/entities/User.java)

### Mapping and DTOs
- [`EventMapper`](src/main/java/com/example/ticket/mappers/EventMapper.java)
- [`CreateEventRequestDto`](src/main/java/com/example/ticket/domain/dtos/CreateEventRequestDto.java)
- [`CreateTicketTypeRequestDto`](src/main/java/com/example/ticket/domain/dtos/CreateTicketTypeRequestDto.java)

---

## Local services and links

| Service | URL | Notes |
|---|---|---|
| App | [http://localhost:8081](http://localhost:8081) | Spring Boot API |
| Adminer | [http://localhost:8888](http://localhost:8888) | PostgreSQL browser |
| Keycloak | [http://localhost:9090](http://localhost:9090) | Identity provider |
| Keycloak Admin Console | [http://localhost:9090/admin/](http://localhost:9090/admin/) | Default local admin UI |
| OIDC well-known config | [http://localhost:9090/realms/event-ticket-platform/.well-known/openid-configuration](http://localhost:9090/realms/event-ticket-platform/.well-known/openid-configuration) | Must resolve for JWT auth |

---

## Prerequisites

Install these before running the project:

- **JDK 21**
- **Docker Desktop**
- **Git**
- **A REST client** like Postman, Bruno, Insomnia, or `curl`

Ports used by default:

- `8081` - application
- `5433` - PostgreSQL (host)
- `8888` - Adminer
- `9090` - Keycloak

---

## How to run locally

### 1) Start infrastructure

```powershell
docker compose up -d db adminer keycloak
docker compose ps
```

### 2) Verify PostgreSQL and Keycloak are reachable

```powershell
curl.exe http://localhost:9090/
```

Optional OIDC verification:

```powershell
curl.exe http://localhost:9090/realms/event-ticket-platform/.well-known/openid-configuration
```

### 3) Run the app

```powershell
.\mvnw.cmd spring-boot:run
```

### 4) Open the local tools

- App: <http://localhost:8081>
- Adminer: <http://localhost:8888>
- Keycloak: <http://localhost:9090>

### 5) Adminer login

Use these values in Adminer:

- **System:** `PostgreSQL`
- **Server:** `db`
- **Username:** `postgres`
- **Password:** `Mnbvcxz@123`
- **Database:** `postgres`

---

## Authentication and security

This project uses **Spring Security + OAuth2 Resource Server**.

### How auth works

- Every API route is currently protected.
- JWT validation is configured with:
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/event-ticket-platform`
- The API expects a **Bearer token** issued by the configured Keycloak realm.

### User provisioning flow

`UserProvisioningFilter` reads the authenticated JWT and stores the user locally if they do not already exist.

Claims used:
- `sub` -> local user ID (`UUID`)
- `preferred_username` -> user name
- `email` -> user email

### Security behavior to know

- CSRF is disabled (stateless API style)
- Session creation policy is `STATELESS`
- Authentication is required for all requests

---

## Validation and error handling

### Request validation

Validation is enforced using Jakarta Bean Validation.

Examples already in the codebase:

- `CreateEventRequestDto.name` -> required
- `CreateEventRequestDto.venue` -> required
- `CreateEventRequestDto.status` -> required
- `CreateEventRequestDto.ticketTypes` -> must not be empty
- `CreateTicketTypeRequestDto.name` -> required
- `CreateTicketTypeRequestDto.price` -> required and non-negative
- `CreateTicketTypeRequestDto.totalAvailable` -> non-negative

### Error handling

`GlobalExceptionHandler` currently handles:

- `UserNotFoundException` -> `404`
- `MethodArgumentNotValidException` -> `400`
- `ConstraintViolationException` -> `400`
- fallback `Exception` -> `500`

Note: some Jackson parsing errors currently fall through the generic handler and return a server error; improving payload error mapping would be a good next enhancement.

---

## API overview

### Base path

`/api/v1/events`

### Implemented endpoints

#### Create event

**POST** `/api/v1/events`

Creates an event for the authenticated organizer and persists nested ticket types.

Example request:

```json
{
  "name": "Spring Music Night",
  "start": "2026-03-20T18:00:00",
  "end": "2026-03-20T22:00:00",
  "venue": "City Arena",
  "salesStart": "2026-03-14T10:00:00",
  "salesEnd": "2026-03-20T17:00:00",
  "status": "DRAFT",
  "ticketTypes": [
    {
      "name": "General Admission",
      "price": 499.0,
      "description": "Main floor access",
      "totalAvailable": 121333123442
    }
  ]
}
```

Example call:

```powershell
curl.exe -X POST http://localhost:8081/api/v1/events ^
  -H "Authorization: Bearer <access_token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Spring Music Night\",\"start\":\"2026-03-20T18:00:00\",\"end\":\"2026-03-20T22:00:00\",\"venue\":\"City Arena\",\"salesStart\":\"2026-03-14T10:00:00\",\"salesEnd\":\"2026-03-20T17:00:00\",\"status\":\"DRAFT\",\"ticketTypes\":[{\"name\":\"General Admission\",\"price\":499.0,\"description\":\"Main floor access\",\"totalAvailable\":121333123442}]}"
```

#### List organizer events

**GET** `/api/v1/events`

Returns a paginated list of events owned by the authenticated organizer.

Example:

```powershell
curl.exe -X GET "http://localhost:8081/api/v1/events?page=0&size=10&sort=createdAt,desc" ^
  -H "Authorization: Bearer <access_token>"
```


## QR code and ticket validation support

The repo already contains the core domain types for QR-based ticket workflows:

- [`QrCode`](src/main/java/com/example/ticket/domain/entities/QrCode.java)
- [`QrCodeStatusEnum`](src/main/java/com/example/ticket/domain/entities/QrCodeStatusEnum.java)
- [`TicketValidation`](src/main/java/com/example/ticket/domain/entities/TicketValidation.java)
- [`TicketValidationMethod`](src/main/java/com/example/ticket/domain/entities/TicketValidationMethod.java)
- [`TicketValidationStatusEnum`](src/main/java/com/example/ticket/domain/entities/TicketValidationStatusEnum.java)

### Current domain semantics

- QR code statuses: `ACTIVE`, `EXPIRED`
- validation methods: `QR_SCAN`, `MANUAL`
- validation statuses: `VALID`, `INVALID`, `EXPIRED`

### Important note

The **entities are present**, but the actual **QR generation service**, **QR delivery**, and **validation endpoints** are not implemented yet in the current public API.

So for GitHub/repo accuracy:
- ✅ domain support exists
- ⚠️ business flow/API exposure is still roadmap

---

## Observability and metrics

### Available today

- standard Spring Boot startup/runtime logs
- Hibernate SQL logging enabled via:
  - `spring.jpa.show-sql=true`
  - `spring.jpa.properties.hibernate.format_sql=true`
- JPA schema auto-update via `spring.jpa.hibernate.ddl-auto=update`

### Not available yet

- no Spring Boot Actuator dependency
- no `/actuator` endpoints
- no Micrometer/Prometheus/Grafana setup
- no custom business metrics for inventory, sales, validation scans, or auth failures

### Suggested next observability upgrades

- Spring Boot Actuator
- Micrometer + Prometheus
- endpoint latency metrics
- DB connection pool metrics
- ticket inventory / purchase counters
- QR validation success/failure metrics

---

## Recommended tools

### Required

- **Java 21**
- **Docker Desktop**
- **Maven Wrapper** (`./mvnw` or `mvnw.cmd`)

### Strongly recommended

- **IntelliJ IDEA** for Spring/JPA development
- **Postman / Bruno / Insomnia** for API testing
- **Adminer** for DB inspection
- **Keycloak Admin Console** for auth setup

### Useful local workflow

- use **Adminer** to verify schema/table creation
- use **Keycloak** to inspect realms/users/tokens
- use **curl/Postman** to test secured endpoints
- use IDE + Hibernate SQL logs to debug persistence issues fast

---

## Testing

Run tests with:

```powershell
.\mvnw.cmd test
```

If your tests or authenticated requests fail because of auth/bootstrap dependencies:
- make sure PostgreSQL is up
- make sure Keycloak is up
- make sure the configured issuer URI is reachable


---

## Roadmap

### Short-term

- [ ] add realm bootstrap/export for Keycloak
- [ ] implement event update/delete/detail APIs
- [ ] implement public published-event APIs
- [ ] improve request deserialization error handling
- [ ] add integration tests for secured endpoints

### Ticketing roadmap

- [ ] implement ticket purchase workflow
- [ ] generate tickets after purchase
- [ ] generate QR codes for issued tickets
- [ ] validate QR codes at entry gates
- [ ] support manual override validation by staff

### Concurrency-safe ticket buying

This is a very good next feature for this project.

Recommended direction:
- [ ] prevent overselling under concurrent requests
- [ ] use database-level locking or optimistic locking
- [ ] reserve inventory before payment confirmation
- [ ] make purchase operations transactional and idempotent
- [ ] record purchase attempts for audit/debugging

A strong future design would include:
- inventory counters per `TicketType`
- transactional purchase service
- retry-safe payment callback handling
- unique/idempotency keys for checkout operations

### Observability roadmap

- [ ] add Actuator
- [ ] expose health/readiness endpoints
- [ ] add Micrometer metrics
- [ ] add Prometheus + Grafana dashboards
- [ ] track auth, purchase, and QR validation metrics

---

## Troubleshooting

### PostgreSQL connection refused

If startup fails with DB connectivity errors:

```powershell
docker compose up -d db
```

The app expects PostgreSQL at:
- `jdbc:postgresql://localhost:5433/postgres`

### JWT decoder / issuer timeout

If you see errors like:
- `JwtDecoderInitializationException`
- `Unable to resolve the Configuration with the provided Issuer`
- timeout on `/.well-known/openid-configuration`

Then Keycloak is not ready yet.

Start it with:

```powershell
docker compose up -d keycloak
```

### GET not supported on `/api/v1/events`

- `GET /api/v1/events` is valid
- `POST /api/v1/events` is valid
- other methods like unsupported GET/POST combinations on the wrong route will fail as expected

### Large `totalAvailable` values

`totalAvailable` is now modeled as `Long`, so large values are supported in the API and domain model.

If your existing database column was created as `INTEGER`, update it to `BIGINT` if Hibernate does not migrate it automatically.

Example SQL:

```sql
alter table ticket_types
alter column total_available type bigint;
```

### PostgreSQL timezone issue on Windows

The app sets JVM default timezone to `Asia/Kolkata` during startup to avoid PostgreSQL rejecting legacy aliases like `Asia/Calcutta`.

---

If you’re planning to add the concurrency-safe ticket buying flow next, this repo is already a solid base for it.
The next clean milestone would be: **purchase API -> transactional inventory lock -> ticket issuance -> QR generation -> validation flow**.

