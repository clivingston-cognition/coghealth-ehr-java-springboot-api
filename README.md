# MedChart EHR API

Electronic Health Record System API for MedChart Health Systems.

## Overview

This is the backend API for the MedChart EHR system, providing RESTful endpoints for patient management, clinical documentation, orders, and integrations.

## Tech Stack

- Java 17
- Spring Boot 2.7.x
- PostgreSQL (Neon for cloud, Docker for local)
- Redis (caching)
- RabbitMQ (messaging)

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.6+
- Docker (for local infrastructure)

### Development Profiles

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | Neon (cloud) | Remote/Devin sessions, shared data |
| `local` | Docker PostgreSQL | Local-only development |

### Quick Start (Recommended - Cloud DB)

Run with the shared Neon database (no local setup needed):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This connects to the shared Neon PostgreSQL instance. Schema and seed data are already applied.

### Local Development (Docker)

For fully local development with Docker:

```bash
# Start infrastructure (PostgreSQL, Redis, etc.)
docker-compose up -d

# Run the app
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## API Documentation

Swagger UI available at: `http://localhost:8080/api/swagger-ui.html`

## Project Structure

```
src/main/java/com/medchart/ehr/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── domain/          # JPA entities
├── dto/             # Data transfer objects
├── mapper/          # Entity-DTO mappers
├── repository/      # JPA repositories
├── service/         # Business logic
├── audit/           # Audit logging
├── security/        # Security configuration
└── legacy/          # Legacy services (deprecated)
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| NEON_DB_PASSWORD | Neon database password | (set in application-dev.yml) |
| DB_PASSWORD | Local database password | medchart_dev |
| JWT_SECRET | JWT signing secret | (dev default) |
| INSURANCE_API_URL | Insurance API endpoint | http://localhost:8081 |
| PHARMACY_API_URL | Pharmacy API endpoint | http://localhost:8082 |

## Database

### Neon (Cloud - dev profile)

The `dev` profile uses a shared Neon PostgreSQL database. Connection details are in `application-dev.yml`. Flyway migrations run automatically on startup.

### Docker (Local - local profile)

Local Docker setup includes:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Keycloak (port 8180)
- RabbitMQ (ports 5672, 15672)
- Elasticsearch (port 9200)
- Kibana (port 5601)

See `docker-compose.yml` for details.

## Contributing

See CONTRIBUTING.md for guidelines.

## License

Proprietary - MedChart Health Systems
