# Todo DDD Spring Boot - AI Agent Instructions

## Project Overview
- Domain-Driven Design (DDD) implementation of a Todo application using Spring Boot 3.2.1 and Java 21
- Focuses on clean architecture, SOLID principles, and extensive testing
- Demonstrates professional microservices patterns and monitoring solutions
- Follows standard Maven project structure with DDD organization

### Maven Project Structure
```
src/
├── main/
│   ├── java/                      # Source code
│   │   └── com/example/todo/      # Root package
│   └── resources/                 # Resources
│       ├── application.yml        # Spring Boot config
│       └── db/migration/          # Flyway migrations
└── test/
    ├── java/                      # Test code
    │   └── com/example/todo/      # Test packages mirror main
    └── resources/                 # Test resources
```

## Key Architectural Patterns

### DDD Layer Structure (within src/main/java/com/example/todo/)
```
├── domain/         # Core business logic
│   ├── model/     # Entities, value objects
│   ├── events/    # Domain events
│   ├── repository/# Repository interfaces
│   └── service/   # Domain services
├── application/   # Use cases, orchestration
│   ├── dto/      # Data transfer objects
│   └── usecase/  # Application services
└── infrastructure/# Technical implementations
    ├── config/   # Spring configurations
    ├── events/   # Event handlers
    ├── metrics/  # Monitoring setup
    ├── persistence/# Repository implementations
    └── rest/     # API controllers
```

### Critical Domain Concepts
- **Todo**: Aggregate root managing lifecycle, status transitions, and events
- **TodoId**: Value object for unique identification
- **Domain Events**: Used for decoupled communication (e.g., `TodoCompletedEvent`)
- **Specifications**: Encapsulate complex query conditions

## Development Workflows

### Building the Project
```bash
# Windows
./build.cmd

# Linux/Mac
./build.sh

# Manual Maven build
./mvnw clean install
```

### Running Tests
```bash
./mvnw test                # Unit tests
./mvnw verify             # Integration tests
```

## Project-Specific Conventions

### Domain Model Patterns
1. Aggregate roots must:
   - Validate business rules internally
   - Emit domain events for state changes
   - Example: `Todo.complete()` validates state and emits `TodoCompletedEvent`

2. Value objects should be immutable:
   ```java
   public record TodoId(String value) {
       public static TodoId generate() {
           return new TodoId(UUID.randomUUID().toString());
       }
   }
   ```

3. Domain events must:
   - Be immutable records
   - Include `aggregateId` and `occurredOn` timestamp
   - Use past tense naming (e.g., `TodoCompletedEvent`)

### Testing Patterns
1. Unit tests should:
   - Test domain logic in isolation
   - Use descriptive `@DisplayName` annotations
   - Follow Given/When/Then pattern

2. Integration tests should:
   - Use `@Testcontainers` for real databases
   - Verify repository implementations
   - Test complete use case flows

## Integration Points

### External Dependencies
- PostgreSQL 15 for persistent storage
- Redis 7 for caching
- Prometheus for metrics collection

### Cross-Component Communication
1. Domain events for async workflows:
   - `TodoEventHandler` processes domain events
   - Events are published via Spring's event system

2. REST API conventions:
   - Base path: `/api/v1/todos`
   - Required headers: `X-User-ID`
   - Responses use `TodoResponse` DTOs

3. Metrics integration:
   - Custom business metrics in `TodoMetrics`
   - Standard Spring Boot actuator endpoints

## Key Files for Common Tasks

### Business Logic
- `Todo.java`: Core domain entity with business rules
- `TodoDomainService.java`: Complex domain operations
- `TodoSpecifications.java`: Reusable query conditions

### Configuration
- `application.yml`: Main Spring Boot config
- `SecurityConfig.java`: Authentication setup
- `CacheConfig.java`: Redis caching config

### Testing
- `ArchitectureTest.java`: Enforces DDD patterns
- `TodoTest.java`: Domain logic unit tests
- `TodoRepositoryIntegrationTest.java`: DB integration