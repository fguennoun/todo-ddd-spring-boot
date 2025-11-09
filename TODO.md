# Domain-Driven Design (DDD) with Spring Boot - Comprehensive Guide

## Table of Contents
1. [Domain-Driven Design Overview](#domain-driven-design-overview)
2. [Project Structure](#project-structure)
3. [Technologies Stack](#technologies-stack)
4. [Implementation Details](#implementation-details)
5. [Testing Strategy](#testing-strategy)
6. [Infrastructure Setup](#infrastructure-setup)
7. [Running the Project](#running-the-project)
8. [Best Practices](#best-practices)

## Domain-Driven Design Overview

### Core DDD Concepts
- **Ubiquitous Language**: Common language used across the project (Todo, Priority, Status)
- **Bounded Contexts**: Clear boundaries between different parts of the application
- **Aggregates**: Todo as the main aggregate root
- **Value Objects**: TodoId, Priority, Status
- **Domain Events**: TodoCreatedEvent, TodoCompletedEvent, etc.
- **Repositories**: Interface defined in domain, implemented in infrastructure

### DDD Layers
1. **Domain Layer** (`domain/`)
   - Heart of the business logic
   - Pure domain objects and business rules
   - No dependencies on external frameworks
   - Contains interfaces (ports) for infrastructure

2. **Application Layer** (`application/`)
   - Orchestrates use cases
   - Coordinates domain objects
   - Manages transactions
   - Handles application-specific logic

3. **Infrastructure Layer** (`infrastructure/`)
   - Implements technical capabilities
   - Database persistence
   - Event publishing
   - External integrations
   - Framework configurations

4. **Interface Layer** (`rest/`)
   - REST API controllers
   - Request/Response DTOs
   - Input validation
   - API documentation

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/todo/
│   │       ├── domain/
│   │       │   ├── model/         # Domain entities and value objects
│   │       │   ├── events/        # Domain events
│   │       │   ├── repository/    # Repository interfaces
│   │       │   └── service/       # Domain services
│   │       ├── application/
│   │       │   ├── dto/          # Data Transfer Objects
│   │       │   └── usecase/      # Application services
│   │       └── infrastructure/
│   │           ├── config/       # Spring configurations
│   │           ├── events/       # Event handlers
│   │           ├── metrics/      # Monitoring
│   │           ├── persistence/  # Repository implementations
│   │           └── rest/        # API controllers
│   └── resources/
│       ├── application.yml      # Application configuration
│       └── db/migration/       # Flyway migrations
└── test/
    └── java/                   # Test classes
```

## Technologies Stack

### Core Framework
- **Spring Boot 3.2.1**: Main application framework
- **Java 21**: Programming language with latest features
- **Spring Data JPA**: Data access and persistence
- **Spring Security**: Authentication and authorization
- **Spring Cache**: Caching support
- **Spring WebMVC**: REST API support

### Data Storage
- **PostgreSQL 15**: Primary database
- **Redis 7**: Caching layer
- **Flyway**: Database migration tool

### Testing
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **TestContainers**: Integration testing with real databases
- **ArchUnit**: Architecture testing
- **Spring Boot Test**: Integration testing support

### Monitoring & Metrics
- **Micrometer**: Application metrics
- **Prometheus**: Metrics storage
- **Grafana**: Metrics visualization

### Documentation
- **SpringDoc OpenAPI**: API documentation
- **Swagger UI**: API explorer

### Infrastructure
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration
- **Maven**: Build tool and dependency management

## Implementation Details

### Domain Model

#### Todo Aggregate
```java
public class Todo {
    private TodoId id;
    private String title;
    private String description;
    private TodoStatus status;
    private TodoPriority priority;
    private String userId;
    private LocalDateTime dueDate;
    // ... business methods
}
```

### Value Objects
```java
public record TodoId(String value) {
    public static TodoId generate() {
        return new TodoId(UUID.randomUUID().toString());
    }
}
```

### Domain Events
```java
public record TodoCompletedEvent(
    TodoId aggregateId,
    String userId,
    LocalDateTime occurredOn
) implements DomainEvent {}
```

## Testing Strategy

### Unit Tests
- Domain model behavior
- Use case orchestration
- Pure business logic
- No infrastructure dependencies

### Integration Tests
- Repository implementations
- Database operations
- Cache interactions
- API endpoints

### Architecture Tests
- Package dependencies
- Layer isolation
- Domain model rules
- Aggregate boundaries

## Infrastructure Setup

### Docker Services
```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: tododb
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
```

## Running the Project

### Prerequisites
- Java 21
- Docker Desktop
- Maven (or use included wrapper)

### Steps to Run

1. Start infrastructure:
```bash
docker compose up -d
```

2. Clear Redis cache:
```bash
docker exec -i todo-redis redis-cli -a redis_password FLUSHALL
```

3. Run application:
```bash
./mvnw -DskipTests -Dspring-boot.run.profiles=local spring-boot:run
```

4. Run tests:
```bash
./mvnw test
```

### API Examples

Create Todo:
```bash
curl -X POST http://localhost:8080/api/v1/todos \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "title": "Learn DDD",
    "description": "Study Domain-Driven Design patterns",
    "priority": "HIGH"
  }'
```

## Best Practices

### Domain Model
1. Encapsulate business rules within aggregates
2. Use value objects for immutable concepts
3. Raise domain events for state changes
4. Keep aggregates boundaries consistent

### Testing
1. Use Test-Driven Development (TDD)
2. Test business rules in isolation
3. Use integration tests for infrastructure
4. Verify architecture with ArchUnit

### Infrastructure
1. Use dependency injection
2. Keep infrastructure concerns separate
3. Use interfaces for external dependencies
4. Cache appropriate data with Redis

### Monitoring
1. Track business metrics
2. Monitor application health
3. Set up alerting
4. Use structured logging