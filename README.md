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

## Table of Contents

- [Current Implementation Status](#current-implementation-status)
- [What This Project Does](#what-this-project-does)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Repository Links](#repository-links)
- [Architecture Snapshot](#architecture-snapshot)
- [Authentication and Security](#authentication-and-security)
- [Validation and Error Handling](#validation-and-error-handling)
- [API Overview](#api-overview)
- [QR Code Generation and Validation](#qr-code-generation-and-validation)
- [Concurrency Implementation](#concurrency-implementation)
- [Local Setup](#local-setup)
- [Build and Test](#build-and-test)
- [Roadmap](#roadmap)
- [Troubleshooting](#troubleshooting)

---

## Current Implementation Status

Implemented and highlighted in current design:

- JWT authentication and authorization (OAuth2 Resource Server + Keycloak)
- Validation APIs and centralized exception handling
- Event, ticket, purchase, QR retrieval, and ticket validation API layers
- QR code generation service flow (`QrCodeService`)
- Concurrency-aware purchase flow with transactional availability checks and sold-out protection
- 5 controllers and 5 services wired in architecture

---

## What This Project Does

This backend supports an event-ticketing workflow where authenticated users can:

- Create and manage organizer events
- Purchase tickets for ticket types
- Retrieve ticket details and QR images
- Validate tickets via QR scan or manual mode

Core domain entities:

- `Event`
- `TicketType`
- `Ticket`
- `QrCode`
- `TicketValidation`
- `User`

---

## Features

- Spring Security protected APIs using JWT bearer tokens
- Keycloak issuer-based JWT verification
- Automatic user provisioning from JWT claims (`sub`, `preferred_username`, `email`)
- DTO-level Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@NotEmpty`, `@PositiveOrZero`)
- Global exception handling via `GlobalExceptionHandler`
- Event CRUD-style API surface for organizer workflows
- Ticket purchase endpoint per ticket type
- Ticket listing/detail/QR retrieval endpoints
- Ticket validation endpoint supporting `QR_SCAN` and `MANUAL`
- QR generation via ZXing
- JPA/Hibernate persistence with PostgreSQL

---

## Tech Stack

- Java 21
- Spring Boot 4 (Web MVC, Security, Data JPA, Validation)
- Spring Security OAuth2 Resource Server (JWT)
- PostgreSQL + Hibernate
- MapStruct + Lombok
- ZXing (QR generation)
- Docker Compose + Adminer
- Keycloak

---

## Repository Links

### Core

- `pom.xml`
- `docker-compose.yml`
- `src/main/resources/application.properties`
- `src/main/java/com/example/ticket/TicketApplication.java`

### Config and security

- `src/main/java/com/example/ticket/config/SecurityConfig.java`
- `src/main/java/com/example/ticket/config/JpaConfiguration.java`
- `src/main/java/com/example/ticket/config/QrCodeConfig.java`
- `src/main/java/com/example/ticket/filters/UserProvisioningFilter.java`
- `src/main/java/com/example/ticket/util/JwtUtil.java`

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

Implementations:

- `src/main/java/com/example/ticket/services/impl/EventServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/TicketServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/TicketTypeServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/QrCodeServiceImpl.java`
- `src/main/java/com/example/ticket/services/impl/TicketValidationServiceImpl.java`

### Mappers and DTOs

- `src/main/java/com/example/ticket/mappers/EventMapper.java`
- `src/main/java/com/example/ticket/mappers/TicketMapper.java`
- `src/main/java/com/example/ticket/mappers/TicketValidationMapper.java`
- `src/main/java/com/example/ticket/domain/dtos/`

### Persistence

- `src/main/java/com/example/ticket/repositories/EventRepository.java`
- `src/main/java/com/example/ticket/repositories/TicketRepository.java`
- `src/main/java/com/example/ticket/repositories/TicketTypeRepository.java`
- `src/main/java/com/example/ticket/repositories/QrCodeRepository.java`
- `src/main/java/com/example/ticket/repositories/UserRepository.java`

---

## Architecture Snapshot

Request flow:

1. Client sends bearer token
2. Spring Security validates JWT issuer/signature
3. `UserProvisioningFilter` ensures local user row exists
4. Controller validates payload
5. Mapper converts DTOs to domain models
6. Service applies business rules
7. Repository persists/fetches from PostgreSQL

---

## Authentication and Security

- All endpoints require authentication.
- JWT issuer is configured in `application.properties`:
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/event-ticket-platform`
- Session policy is stateless.
- CSRF is disabled for API usage.

User provisioning claims:

- `sub` -> user UUID
- `preferred_username` -> name
- `email` -> email

---

## Validation and Error Handling

Validation is enforced in request DTOs with Jakarta Bean Validation.

Centralized exception handling is in:

- `src/main/java/com/example/ticket/controllers/GlobalExceptionHandler.java`

Handled categories include:

- user-not-found
- method argument validation failures
- constraint violations
- fallback internal exception response

---

## API Overview

### Event APIs

- `POST /api/v1/events`
- `PUT /api/v1/events/{eventId}`
- `GET /api/v1/events`
- `GET /api/v1/events/{eventId}`
- `DELETE /api/v1/events/{eventId}`

### Ticket purchase API

- `POST /api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets`

### Ticket APIs

- `GET /api/v1/tickets`
- `GET /api/v1/tickets/{ticketId}`
- `GET /api/v1/tickets/{ticketId}/qr-codes`

### Ticket validation API

- `POST /api/v1/ticket-validations`
  - mode: `QR_SCAN`
  - mode: `MANUAL`

---

## QR Code Generation and Validation

Implemented in current design:

- QR generation service method: `QrCodeServiceImpl.generateQrCode(...)`
- Ticket QR retrieval endpoint: `GET /api/v1/tickets/{ticketId}/qr-codes`
- Ticket validation endpoint: `POST /api/v1/ticket-validations`

QR/validation enums:

- `QrCodeStatusEnum`: `ACTIVE`, `EXPIRED`
- `TicketValidationMethod`: `QR_SCAN`, `MANUAL`
- `TicketValidationStatusEnum`: `VALID`, `INVALID`, `EXPIRED`

---

## Concurrency Implementation

Concurrency-aware ticket buying is implemented using transactional purchase flow and availability checks:

- Count sold tickets for a ticket type
- Compare against `totalAvailable`
- Block purchase by throwing sold-out exception when exhausted

Next-level hardening (future):

- explicit row-level locking or optimistic versioning for high contention
- reservation windows and idempotent purchase semantics

---

## Local Setup

### Prerequisites

- JDK 21
- Docker Desktop
- Git
- REST client (Postman, Bruno, Insomnia, curl)

### Start local infra

```powershell
docker compose up -d db adminer keycloak
docker compose ps
```

### Run API

```powershell
.\mvnw.cmd spring-boot:run
```

### Local endpoints

- API: `http://localhost:8081`
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

## Build and Test

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test
```

---

## Roadmap

- Lock-based oversell protection for high-contention traffic
- Deeper purchase observability and audit events
- Enhanced payload coercion/error contracts
- Published-event discovery/search hardening
- Actuator + Micrometer + dashboard integration

---

## Troubleshooting

### Keycloak issuer/JWT decoder timeout

```powershell
docker compose up -d keycloak
```

### PostgreSQL connection errors

```powershell
docker compose up -d db
```

### Timezone alias issue (`Asia/Calcutta`)

App startup normalizes timezone to `Asia/Kolkata` to avoid PostgreSQL alias failures.

### Large inventory values

`totalAvailable` is modeled as `Long`; if schema is older and integer-based, migrate to `BIGINT`.
