# MedChart EHR API

Electronic Health Record System API for MedChart Health Systems.

## Overview

This is the backend API for the MedChart EHR system, providing RESTful endpoints for patient management, clinical documentation, orders, and integrations.

## Tech Stack

- Java 11
- Spring Boot 2.7.x
- PostgreSQL
- Redis (caching)
- RabbitMQ (messaging)

## Getting Started

### Prerequisites

- JDK 11+
- Maven 3.6+
- PostgreSQL 14+
- Redis 7+
- Docker (optional)

### Local Development

1. Clone the repository
2. Copy `application-local.yml.example` to `application-local.yml` and configure
3. Start PostgreSQL and Redis
4. Run the application:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Docker

```bash
docker-compose up -d
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
| DB_PASSWORD | Database password | medchart_dev |
| JWT_SECRET | JWT signing secret | (dev default) |
| INSURANCE_API_URL | Insurance API endpoint | http://localhost:8081 |
| PHARMACY_API_URL | Pharmacy API endpoint | http://localhost:8082 |

## Contributing

See CONTRIBUTING.md for guidelines.

## License

Proprietary - MedChart Health Systems
