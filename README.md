# MedChart EHR API

Electronic Health Record System API for MedChart Health Systems.

## Overview

This is the backend API for the MedChart EHR system, providing RESTful endpoints for patient management, clinical documentation, orders, and integrations.

## Tech Stack

- Java 11
- Spring Boot 2.7.x
- PostgreSQL (Neon for cloud, Docker for local)
- Redis (caching)
- RabbitMQ (messaging)

## Getting Started

### Prerequisites

- JDK 11+
- Maven 3.6+
- Docker (for local infrastructure)

### Development Profiles

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | Neon (cloud) | Remote/Devin sessions, shared data |
| `local` | Docker PostgreSQL | Local-only development |

### Environment Setup

#### Local Development

1. Create a `.env` file in the project root:

```bash
# Neon (dev profile)
NEON_DB_URL=jdbc:postgresql://<your-neon-endpoint>/neondb?sslmode=require
NEON_DB_USERNAME=<your-neon-username>
NEON_DB_PASSWORD=<your-neon-password>

# Local DB (default profile)
DB_URL=jdbc:postgresql://localhost:5432/medchart_ehr
DB_USERNAME=medchart
DB_PASSWORD=medchart_dev
```

2. Source the env file or use a tool like `direnv`:

```bash
export $(cat .env | xargs)
```

#### GitHub Actions (CI)

Add the following **repository secrets** at `Settings → Secrets and variables → Actions → Repository secrets`:

| Secret | Description |
|--------|-------------|
| `NEON_DB_URL` | Full JDBC URL for Neon (e.g., `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require`) |
| `NEON_DB_USERNAME` | Neon database username |
| `NEON_DB_PASSWORD` | Neon database password |

### Quick Start (Recommended - Cloud DB)

Run with the shared Neon database:

```bash
# Ensure env vars are set (see Environment Setup above)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This connects to the shared Neon PostgreSQL instance. Schema and seed data are already applied.

### Local Development (Docker)

For fully local development with Docker:

```bash
# Start infrastructure (PostgreSQL, Redis, etc.)
# Note: docker-compose.yml is in the data/ repo
cd ../data && docker-compose up -d && cd ../api

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

### Neon (dev profile)

| Variable | Description | Required |
|----------|-------------|----------|
| `NEON_DB_URL` | Full JDBC connection URL | Yes |
| `NEON_DB_USERNAME` | Database username | Yes |
| `NEON_DB_PASSWORD` | Database password | Yes |

### Local (default profile)

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/medchart_ehr` |
| `DB_USERNAME` | Database username | `medchart` |
| `DB_PASSWORD` | Database password | `medchart_dev` |

### Other

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret | (dev default) |
| `INSURANCE_API_URL` | Insurance API endpoint | `http://localhost:8081` |
| `PHARMACY_API_URL` | Pharmacy API endpoint | `http://localhost:8082` |

## Database

### Neon (Cloud - dev profile)

The `dev` profile uses a shared Neon PostgreSQL database. Credentials are loaded from environment variables. Flyway migrations run automatically on startup.

### Docker (Local - local profile)

Local Docker setup (in `data/` repo) includes:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Keycloak (port 8180)
- RabbitMQ (ports 5672, 15672)
- Elasticsearch (port 9200)
- Kibana (port 5601)

## Contributing

See CONTRIBUTING.md for guidelines.

## Security

**Note:** Authentication is currently disabled for development. All endpoints are accessible without credentials. CORS is configured to allow requests from `localhost:5173`, `localhost:5178`, and `localhost:3000`.

## License

Demo Application - Cognition AI
