# Todo DDD Spring Boot — Local Development & Debugging Guide

This README documents the local development workflow, debugging steps and notable changes made while preparing the project to run locally with containerized infrastructure (Postgres, Redis, Prometheus, Grafana, Zipkin).

Overview
--------
- Java 21, Spring Boot 3.2.x
- Postgres 15 (container)
- Redis 7 (container)
- Flyway for database migrations
- Actuator + Micrometer (Prometheus)
- SpringDoc OpenAPI for API docs

What I changed and why
----------------------
- docker-compose.yml: The `app` service is commented out so you can run the application locally while the other services run in Docker.
- `application.yml`: added a `local` profile with sensible defaults (DB, Redis, actuator) to avoid exporting many environment variables.
- Security: added a `SecurityConfigLocal` (profile `local`) that relaxes authentication for local testing, and annotated the main `SecurityConfig` with `@Profile("!local")` so production behavior is preserved.
- Flyway migration: fixed an invalid partial index predicate that used non-immutable functions (removed CURRENT_TIMESTAMP usage) so migrations run under Postgres.
- Caching: changed Redis cache configuration to use a JSON serializer built from the application's `ObjectMapper` instead of caching framework classes like `PageImpl`.
- Pagination: added/used a JSON-friendly `PageResult<T>` (domain DTO) so responses are safe to serialize to JSON and cache in Redis.
- Jackson: added `jackson-datatype-jsr310` and registered `JavaTimeModule` via `JacksonConfig` to ensure `Instant` is serialized/deserialized correctly.

Quick start (local development)
-------------------------------
1. Start Docker Desktop.
2. Start infrastructure containers from the project root:

```bash
docker compose up -d postgres redis prometheus grafana zipkin
```

3. (Optional but recommended) Flush Redis after switching serializers to remove incompatible cached payloads:

```bash
docker exec -i todo-redis redis-cli -a redis_password FLUSHALL
```

4. Start the application locally with the `local` Spring profile (this uses DB/Redis values from `application.yml`):

```bash
./mvnw -DskipTests -Dspring-boot.run.profiles=local spring-boot:run
```

If port 8080 is in use, run with a different port:

```bash
./mvnw -DskipTests -Dspring-boot.run.profiles=local -Dserver.port=8081 spring-boot:run
```

5. API access
- Open Swagger UI: http://localhost:8080/swagger-ui.html (or 8081 if you changed port)
- Use header `X-User-ID` with a user id (e.g., `user123`) when calling the API endpoints. When running with the `local` profile, authentication is relaxed.

Common troubleshooting
----------------------
- MeterRegistry bean missing on startup: ensure `spring-boot-starter-actuator` and micrometer registry are on the classpath (this repo adds them to `pom.xml`).
- Flyway failing: check `src/main/resources/db/migration` for SQL migration syntax. The V1 migration was updated to remove non-immutable functions in index predicates.
- Redis serialization errors: if you switch serializer implementations, flush Redis to avoid deserialization errors from old payloads.
- `Instant` serialization errors: ensure `jackson-datatype-jsr310` is available and `JacksonConfig` registers the `JavaTimeModule` and disables timestamps.

Running tests
-------------
- Unit tests: run `./mvnw test` (runs unit tests via Surefire).
- Integration tests: run `./mvnw verify` (this may use Testcontainers or require Docker running).

Notes for maintainers
---------------------
- Production security and settings are unchanged; local-only changes are isolated behind the `local` Spring profile.
- Be careful when changing Redis serializers in the future — coordinate cache invalidation or use versioned cache keys.

If anything in this README is unclear or you want a shorter summary for the repo root, I can produce a trimmed README or add a developer.md with step-by-step screenshots.

— End of automated summary
# 🎯 Todo DDD Spring Boot - Application de Référence

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Vue d'Ensemble

Cette application **Todo List** sert de **référence complète** pour les bonnes pratiques du développement backend moderne avec **Domain-Driven Design (DDD)**. Elle démontre l'implémentation professionnelle d'une architecture microservices avec Spring Boot 3.x et Java 21.

### 🎯 Objectifs Pédagogiques

- **Architecture DDD** : Implémentation complète des patterns et concepts DDD
- **Bonnes Pratiques** : Code production-ready avec tests complets
- **Stack Moderne** : Java 21, Spring Boot 3.x, PostgreSQL, Redis
- **DevOps** : Docker, CI/CD, monitoring, observabilité
- **Sécurité** : Authentification JWT, scans de sécurité, HTTPS
- **Performance** : Cache Redis, optimisations JVM, métriques

## 🏗️ Architecture DDD

### Structure du Projet

```
src/
├── main/java/com/example/todo/
│   ├── domain/                    # 🏛️ COUCHE DOMAINE
��   │   ├── model/
│   │   │   ├── Todo.java         # Aggregate Root
│   │   │   ├── TodoId.java       # Value Object
│   │   │   ├── TodoStatus.java   # Enumeration
│   │   │   └── TodoPriority.java # Value Object
│   │   ├── events/
│   │   │   ├── DomainEvent.java
│   │   │   ├── TodoCreatedEvent.java
│   │   │   ├── TodoCompletedEvent.java
│   │   │   └── TodoStatusChangedEvent.java
│   │   ├── repository/
│   │   │   └── TodoRepository.java # Interface
│   │   ├── service/
│   │   │   └── TodoDomainService.java
│   │   └── specification/
│   │       ├── Specification.java
│   │       └── TodoSpecifications.java
│   ├── application/               # 📋 COUCHE APPLICATION
│   │   ├── dto/
│   │   │   ├── CreateTodoCommand.java
│   │   │   ├── UpdateTodoCommand.java
│   │   │   └── TodoResponse.java
│   │   └── usecase/
│   │       ├── CreateTodoUseCase.java
│   │       ├── GetTodoUseCase.java
│   │       ├── UpdateTodoUseCase.java
│   │       ├── CompleteTodoUseCase.java
│   │       ├── ListTodosUseCase.java
│   │       └── DeleteTodoUseCase.java
│   └── infrastructure/            # 🔧 COUCHE INFRASTRUCTURE
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   └── CacheConfig.java
│       ├── persistence/
│       │   ├── TodoJpaEntity.java
│       │   ├── TodoJpaRepository.java
│       │   └── TodoRepositoryImpl.java
│       └── rest/
│           ├── TodoController.java
│           └── GlobalExceptionHandler.java
└── test/                          # 🧪 TESTS
    ├── architecture/
    │   └── ArchitectureTest.java  # Tests ArchUnit
    ├── domain/model/
    │   └── TodoTest.java          # Tests unitaires
    ├── application/usecase/
    │   └── CreateTodoUseCaseTest.java
    └── infrastructure/persistence/
        └── TodoRepositoryIntegrationTest.java
```

### 🧱 Building Blocks DDD Implémentés

#### **Aggregate Root - Todo**
```java
public class Todo {
    // Encapsule la logique métier et maintient la cohérence
    public void complete() {
        if (!status.canTransitionTo(TodoStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot complete todo");
        }
        this.status = TodoStatus.COMPLETED;
        addDomainEvent(new TodoCompletedEvent(...));
    }
}
```

#### **Value Objects**
```java
public record TodoId(String value) {
    // Immutable, validation intégrée
    public static TodoId generate() {
        return new TodoId(UUID.randomUUID().toString());
    }
}
```

#### **Domain Events**
```java
public record TodoCreatedEvent(
    TodoId aggregateId,
    String title,
    Instant occurredOn
) implements DomainEvent {
    // Événements métier pour découplage
}
```

## 🚀 Quick Start

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.6+**
- **Docker** et **Docker Compose**

### 🔧 Installation Locale

```bash
# 1. Cloner le projet
git clone https://github.com/example/todo-ddd-springboot-app.git
cd todo-ddd-springboot-app

# 2. Build avec script automatique (recommandé)
# Windows
.\build.cmd

# Linux/Mac  
chmod +x build.sh
./build.sh

# 3. OU build manuel avec Maven (si installé)
mvn clean install
mvn spring-boot:run -Dspring.profiles.active=dev
```

> **📝 Note importante :** Si vous n'avez pas Maven installé, utilisez les scripts `build.cmd` (Windows) ou `build.sh` (Linux/Mac) qui téléchargent automatiquement Maven localement.

### 🐳 Installation Docker Complète

```bash
# Démarrage de toute la stack
docker-compose up -d
```

### 🌐 Accès aux Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **API REST** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Actuator** | http://localhost:8080/actuator | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin/admin123 |
| **Zipkin** | http://localhost:9411 | - |

## 📊 API REST - Exemples d'Utilisation

### Créer un Todo

```bash
curl -X POST http://localhost:8080/api/v1/todos \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "title": "Implémenter les tests unitaires",
    "description": "Créer les tests pour la couche domaine",
    "priority": {
      "level": 3,
      "name": "Haute"
    },
    "dueDate": "2024-12-31T23:59:59Z"
  }'
```

### Lister les Todos avec Pagination

```bash
curl -X GET "http://localhost:8080/api/v1/todos?page=0&size=10&status=PENDING" \
  -H "X-User-ID: user123"
```

### Mettre à Jour un Todo

```bash
curl -X PUT http://localhost:8080/api/v1/todos/{todoId} \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user123" \
  -d '{
    "title": "Nouveau titre",
    "priority": {
      "level": 4,
      "name": "Critique"
    }
  }'
```

### Compléter un Todo

```bash
curl -X POST http://localhost:8080/api/v1/todos/{todoId}/complete \
  -H "X-User-ID: user123"
```

## 🛠️ Stack Technique Complète

### **Backend Core**
- **Java 21** : Records, Pattern Matching, Virtual Threads
- **Spring Boot 3.2.1** : Auto-configuration, Actuator
- **Spring Security** : JWT, CORS, Rate Limiting
- **Spring Data JPA** : Hibernate 6.x, Flyway migrations
- **Spring Cache** : Redis integration

### **Base de Données & Cache**
- **PostgreSQL 15** : Base principale avec index optimisés
- **Redis 7** : Cache multi-niveaux, sessions
- **H2** : Base de test en mémoire

### **Tests & Qualité**
- **JUnit 5** : Tests unitaires et d'intégration
- **Testcontainers** : Tests avec PostgreSQL réel
- **ArchUnit** : Validation architecture DDD
- **JaCoCo** : Couverture de code >80%
- **AssertJ** : Assertions fluides

### **Observabilité & Monitoring**
- **Micrometer** : Métriques Prometheus
- **Spring Cloud Sleuth** : Tracing distribué
- **Zipkin** : Visualisation des traces
- **Logback** : Logs JSON structurés

### **DevOps & Sécurité**
- **Docker** : Multi-stage builds optimisés
- **Jenkins** : Pipeline CI/CD complet
- **SonarQube** : Qualité et sécurité du code
- **OWASP** : Scan des vulnérabilités
- **Trivy** : Sécurité des containers

## 🧪 Stratégie de Tests Complète

### Tests Unitaires (>80% couverture)

```java
@DisplayName("Todo Aggregate")
class TodoTest {
    @Test
    @DisplayName("Should create todo with valid data")
    void shouldCreateTodoWithValidData() {
        // Given, When, Then avec AssertJ
        Todo todo = Todo.create("Title", "Description", PRIORITY, dueDate, USER_ID);
        
        assertThat(todo.getStatus()).isEqualTo(TodoStatus.PENDING);
        assertThat(todo.getAndClearDomainEvents())
            .hasSize(1)
            .first().isInstanceOf(TodoCreatedEvent.class);
    }
}
```

### Tests d'Intégration avec Testcontainers

```java
@Testcontainers
@DataJpaTest
class TodoRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Test
    void shouldPersistAndRetrieveTodo() {
        // Tests réels avec PostgreSQL
    }
}
```

### Tests Architecturaux avec ArchUnit

```java
@ArchTest
static final ArchRule domain_should_not_depend_on_infrastructure = 
    noClasses().that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
```

## 🚀 Pipeline CI/CD Jenkins

### Stages Automatisées

1. **🔄 Checkout** : Code source depuis Git
2. **🧪 Tests Unitaires** : JUnit + JaCoCo
3. **🔧 Tests Intégration** : Testcontainers
4. **📊 Analyse SonarQube** : Quality Gates
5. **🔒 Sécurité** : OWASP + SpotBugs
6. **📦 Build JAR** : Maven package
7. **🐳 Docker Build** : Image multi-stage
8. **🛡️ Scan Trivy** : Sécurité container
9. **📤 Push Registry** : Docker Hub
10. **🚀 Deploy Staging** : Déploiement auto
11. **💨 Smoke Tests** : Vérifications finales

### Quality Gates

- **Couverture** : >80%
- **Duplication** : <3%
- **Complexité** : <15
- **Vulnérabilités** : 0 critiques
- **Technical Debt** : <5%

## 📈 Monitoring & Observabilité

### Métriques Prometheus

```yaml
# Métriques métier disponibles
todo_created_total: Nombre de todos créés
todo_completed_total: Todos complétés
todo_overdue_gauge: Todos en retard
user_productivity_score: Score productivité
```

### Dashboards Grafana Prêts

- **Application Overview** : Santé générale
- **Business Metrics** : KPIs métier
- **Technical Metrics** : Performance JVM
- **Error Tracking** : Suivi des erreurs

### Logs Structurés JSON

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "logger": "CreateTodoUseCase",
  "message": "Creating todo for user: user123",
  "userId": "user123",
  "correlationId": "abc-123",
  "tags": ["todo", "creation"]
}
```

## 🔧 Configuration Avancée

### Profiles Spring

- **dev** : H2, hot reload, debug logs
- **test** : Configuration tests automatisés  
- **prod** : PostgreSQL, cache Redis, optimisations

### Variables d'Environnement

```bash
# Base de données
DATABASE_URL=jdbc:postgresql://localhost:5432/todoapp
DATABASE_USER=todo_user
DATABASE_PASSWORD=todo_password

# Cache Redis
REDIS_URL=redis://localhost:6379
REDIS_PASSWORD=redis_password

# Sécurité JWT
JWT_SECRET=mySecretKey
JWT_EXPIRATION=86400000

# Monitoring
ZIPKIN_URL=http://zipkin:9411/api/v2/spans
```

## 🤝 Contribution & Standards

### Workflow Git Flow

```bash
# 1. Feature branch
git checkout develop
git checkout -b feature/nouvelle-fonctionnalite

# 2. Développement avec TDD
./mvnw test

# 3. Pull Request vers develop
# 4. Review + Pipeline CI/CD
# 5. Merge automatique si qualité OK
```

### Conventions de Code

- **Java 21** avec records et sealed classes
- **Ubiquitous Language** : Vocabulaire métier dans le code
- **Clean Code** : SOLID, DRY, KISS
- **Tests First** : TDD pour toute nouvelle fonctionnalité

### Checklist Pull Request

- [ ] Tests unitaires >80% couverture
- [ ] Tests d'intégration si nécessaire
- [ ] Documentation API à jour
- [ ] Respect architecture DDD
- [ ] Pipeline CI/CD en succès
- [ ] Review approuvée

## 📚 Fondements Théoriques

Cette section explique les concepts et technologies utilisés dans l'application, avec un focus particulier sur **Domain-Driven Design** et **Spring Boot**.

### 🏛️ **Domain-Driven Design (DDD)** ⭐ *Notre Focus Principal*

**Domain-Driven Design** est une approche de développement logiciel qui place le domaine métier au centre de la conception.

#### **Concepts Clés DDD**

**🎯 Ubiquitous Language (Langage Omniprésent)**
- Vocabulaire partagé entre développeurs et experts métier
- Utilisé dans le code, les tests, la documentation
- *Exemple* : `Todo`, `Complete`, `Overdue`, `Priority`

**🏗️ Layered Architecture (Architecture en Couches)**
```
┌─────────────────┐
│  Presentation   │ ← Controllers, API REST
├─────────────────┤
│  Application    │ ← Use Cases, Orchestration
├─────────────────┤
│    Domain       │ ← Business Logic ⭐
├─────────────────┤
│ Infrastructure  │ ← Database, External APIs
└─────────────────┘
```

**🧱 Building Blocks DDD**

- **Entities** : Objets avec identité (ex: `Todo` avec `TodoId`)
- **Value Objects** : Objets définis par leurs valeurs (ex: `TodoPriority`)
- **Aggregates** : Groupes d'entités avec une racine unique
- **Domain Services** : Logique métier entre agrégats
- **Repositories** : Abstraction pour l'accès aux données
- **Domain Events** : Événements métier significatifs

**⚡ Aggregate Pattern**
```java
public class Todo { // Aggregate Root
    private final List<DomainEvent> events = new ArrayList<>();
    
    public void complete() {
        // Validation des règles métier
        if (!canBeCompleted()) {
            throw new BusinessRuleException("Cannot complete");
        }
        
        // Changement d'état
        this.status = COMPLETED;
        
        // Émission d'événement
        addEvent(new TodoCompletedEvent(this.id));
    }
}
```

**🔄 Domain Events Pattern**
- Découplage entre agrégats
- Communication asynchrone
- Traçabilité des changements métier

#### **Avantages du DDD**
- ✅ Alignement code/métier
- ✅ Maintenabilité élevée  
- ✅ Tests focalisés sur la logique métier
- ✅ Évolutivité architecturale

---

### 🚀 **Spring Boot** ⭐ *Notre Focus Principal*

**Spring Boot** simplifie le développement d'applications Spring avec une configuration automatique intelligente.

#### **Concepts Fondamentaux**

**🎯 Auto-Configuration**
```java
@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class TodoApplication {
    // Spring Boot configure automatiquement :
    // - Serveur web (Tomcat)
    // - Base de données (selon driver présent)
    // - Sécurité (si Spring Security présent)
    // - Cache (si Redis présent)
}
```

**📦 Starters (Dépendances Prêtes à l'Emploi)**
- `spring-boot-starter-web` → Tomcat + Spring MVC + Jackson
- `spring-boot-starter-data-jpa` → Hibernate + Spring Data
- `spring-boot-starter-security` → Spring Security + Authentification
- `spring-boot-starter-cache` → Cache abstraction + providers

**🔧 Configuration Externalisée**
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DATABASE_URL:jdbc:h2:mem:testdb}
    username: ${DB_USER:sa}
    password: ${DB_PASSWORD:}
```

**📊 Spring Boot Actuator**
- Health checks : `/actuator/health`
- Métriques : `/actuator/metrics`
- Monitoring : `/actuator/prometheus`
- Configuration : `/actuator/configprops`

---

### 🔐 **Spring Security**

Framework de sécurité complet pour applications Spring.

#### **Concepts Clés**

**🛡️ Security Filter Chain**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/v1/**").authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt())
        .build();
}
```

**🔑 Authentification vs Autorisation**
- **Authentification** : Qui êtes-vous ? (JWT tokens)
- **Autorisation** : Que pouvez-vous faire ? (Roles, permissions)

**🚫 Protection CSRF, CORS, XSS**
- CSRF tokens pour formulaires
- CORS configuration pour API REST
- XSS protection via headers sécurisés

---

### 💾 **Spring Data JPA**

Abstraction puissante pour l'accès aux données relationnelles.

#### **Concepts Fondamentaux**

**🗃️ Repository Pattern**
```java
public interface TodoRepository extends JpaRepository<Todo, Long> {
    // Méthodes générées automatiquement
    Page<Todo> findByUserIdAndStatus(String userId, Status status, Pageable pageable);
    
    // Requêtes personnalisées
    @Query("SELECT t FROM Todo t WHERE t.dueDate < :now AND t.status = 'PENDING'")
    List<Todo> findOverdueTodos(@Param("now") Instant now);
}
```

**🔄 Mapping Objet-Relationnel**
```java
@Entity
@Table(name = "todos")
public class TodoJpaEntity {
    @Id
    private String id;
    
    @Enumerated(EnumType.STRING)
    private TodoStatus status;
    
    @CreatedDate
    private Instant createdAt;
}
```

---

### 🏃‍♂️ **Spring Cache**

Abstraction de cache transparent pour améliorer les performances.

**🎯 Annotations Cache**
```java
@Cacheable(value = "todos", key = "#id")
public Todo findById(String id) {
    // Méthode coûteuse - résultat mis en cache
}

@CacheEvict(value = "todos", key = "#id")  
public void deleteById(String id) {
    // Invalidation du cache
}
```

---

### 🐘 **PostgreSQL**

Base de données relationnelle open-source avancée.

#### **Caractéristiques**

**🎯 ACID Compliance**
- **Atomicity** : Transactions tout-ou-rien
- **Consistency** : Respect des contraintes
- **Isolation** : Transactions isolées
- **Durability** : Persistance garantie

**🚀 Fonctionnalités Avancées**
```sql
-- Index composites optimisés
CREATE INDEX idx_todos_user_status_due ON todos(user_id, status, due_date);

-- Contraintes métier
ALTER TABLE todos ADD CONSTRAINT chk_completed_date 
CHECK ((status = 'COMPLETED' AND completed_at IS NOT NULL) OR 
       (status != 'COMPLETED' AND completed_at IS NULL));
```

---

### 🔥 **Redis 7**

Base de données en mémoire haute performance.

#### **Cas d'Usage dans notre App**

**💨 Cache Application**
```redis
# Cache todos par utilisateur
SET cache:todos:user123 '{"todos": [...], "lastUpdate": "2024-01-15T10:30:00Z"}'
EXPIRE cache:todos:user123 600  # TTL 10 minutes
```

**📊 Sessions Utilisateur**
```redis
# Session tokens JWT
HSET session:abc123 userId user123 roles "USER,ADMIN"
```

---

### 💧 **H2 Database**

Base de données en mémoire pour développement et tests.

**🧪 Configuration Tests**
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop  # Schéma recréé à chaque test
```

---

### 🧪 **JUnit 5**

Framework de test moderne pour Java.

**🎯 Annotations Principales**
```java
@DisplayName("Todo Domain Tests")
class TodoTest {
    
    @Test
    @DisplayName("Should create todo with valid data")
    void shouldCreateTodoWithValidData() {
        // Given
        String title = "Test Todo";
        
        // When
        Todo todo = Todo.create(title, description, priority, dueDate, userId);
        
        // Then
        assertThat(todo.getTitle()).isEqualTo(title);
        assertThat(todo.getStatus()).isEqualTo(PENDING);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void shouldRejectBlankTitles(String blankTitle) {
        assertThatThrownBy(() -> Todo.create(blankTitle, "", NORMAL, null, "user"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

---

### 🐳 **Testcontainers**

Tests d'intégration avec containers Docker réels.

**🎯 Tests avec Vraies Dépendances**
```java
@Testcontainers
class TodoRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("todotest")
        .withUsername("test")
        .withPassword("test");
    
    @Test
    void shouldPersistTodoCorrectly() {
        // Test avec vraie PostgreSQL dans Docker
        Todo todo = repository.save(createTodo());
        assertThat(repository.findById(todo.getId())).isPresent();
    }
}
```

---

### 🏗️ **ArchUnit**

Tests automatisés d'architecture et règles de code.

**🎯 Validation Architecture DDD**
```java
@ArchTest
static final ArchRule domain_should_not_depend_on_infrastructure = 
    noClasses().that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

@ArchTest  
static final ArchRule repositories_should_be_interfaces =
    classes().that().resideInAPackage("..domain.repository..")
    .should().beInterfaces();
```

---

### 📊 **JaCoCo (Java Code Coverage)**

Outil de mesure de couverture de code.

**📊 Métriques JaCoCo**
- **Line Coverage** : % lignes exécutées
- **Branch Coverage** : % branches if/else testées  
- **Method Coverage** : % méthodes appelées
- **Class Coverage** : % classes touchées

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>  <!-- 80% minimum -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

---

### ✅ **AssertJ**

Bibliothèque d'assertions fluides pour tests Java.

**🎯 Syntaxe Fluide**
```java
// AssertJ vs JUnit classique
assertThat(todo.getStatus()).isEqualTo(COMPLETED);           // AssertJ ✅
assertEquals(COMPLETED, todo.getStatus());                   // JUnit ❌

// Collections
assertThat(todos)
    .hasSize(3)
    .extracting(Todo::getTitle)
    .containsExactly("Todo 1", "Todo 2", "Todo 3");

// Exceptions
assertThatThrownBy(() -> todo.complete())
    .isInstanceOf(IllegalStateException.class)
    .hasMessageContaining("cannot complete");
```

---

### 📊 **Micrometer**

Bibliothèque de métriques pour monitoring d'applications.

**📈 Types de Métriques**
```java
@Service
public class TodoMetricsService {
    
    private final Counter todosCreated;
    private final Timer todoCreationTime;
    
    public TodoMetricsService(MeterRegistry registry) {
        this.todosCreated = Counter.builder("todos.created.total")
            .description("Total todos created")
            .register(registry);
            
        this.todoCreationTime = Timer.builder("todos.creation.time")
            .register(registry);
    }
    
    @Timed(value = "todos.creation.time")
    public Todo createTodo(CreateTodoCommand command) {
        todosCreated.increment();
        return todoService.create(command);
    }
}
```

---

### 🔍 **Spring Cloud Sleuth**

Tracing distribué pour applications Spring.

**🏃‍♂️ Suivi Requêtes**
```java
// Automatiquement ajouté à tous les logs
[trace-id,span-id] 2024-01-15 10:30:00 INFO CreateTodoUseCase - Creating todo

// Headers HTTP automatiques
X-Trace-Id: 64c123ab567890cd  
X-Span-Id: ab567890cd123456
```

---

### 🔗 **Zipkin**

Système de tracing distribué pour visualiser les performances.

**📊 Timeline des Requêtes**
```
GET /api/v1/todos/123
├─ TodoController.getTodo()           [2ms]
├─ GetTodoUseCase.execute()          [15ms]  
│  ├─ TodoRepository.findById()       [8ms]
│  │  └─ PostgreSQL Query            [6ms]
│  └─ TodoResponse.from()            [1ms]
└─ Security Filter                   [1ms]
Total: 18ms
```

---

### 📝 **Logback**

Framework de logging performant pour Java.

**📊 Logs JSON Structurés**
```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.example.todo.application.usecase.CreateTodoUseCase",
  "message": "Creating todo for user",
  "mdc": {
    "userId": "user123",
    "traceId": "64c123ab567890cd",
    "spanId": "ab567890cd123456"
  }
}
```

---

### 🐳 **Docker**

Plateforme de containerisation pour déploiement cohérent.

**🏗️ Multi-Stage Build**
```dockerfile
# Stage 1: Build
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
RUN ./mvnw dependency:go-offline

# Stage 2: Runtime  
FROM openjdk:21-jre-slim AS runtime
COPY --from=builder /app/target/todo-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 🏗️ **Jenkins**

Serveur d'intégration continue open-source.

**📜 Pipeline as Code**
```groovy
pipeline {
    agent any
    stages {
        stage('Tests') {
            parallel {
                stage('Unit Tests') {
                    steps { sh './mvnw test' }
                }
                stage('Integration Tests') {
                    steps { sh './mvnw verify' }
                }
            }
        }
        stage('Quality Gate') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar'
                }
            }
        }
    }
}
```

---

### 📊 **SonarQube**

Plateforme d'analyse continue de qualité de code.

**🎯 Quality Gates**
```yaml
Coverage: > 80%                    # Couverture tests
Duplication: < 3%                  # Code dupliqué
Maintainability Rating: A          # Facilité maintenance
Reliability Rating: A              # Fiabilité (bugs)
Security Rating: A                 # Vulnérabilités sécurité
```

---

### 🛡️ **OWASP Dependency-Check**

Scanner de vulnérabilités dans les dépendances.

**🔍 Base Données Vulnérabilités**
- **CVE** (Common Vulnerabilities and Exposures)
- **NVD** (National Vulnerability Database)  
- **GitHub Security Advisories**

**🚨 Niveaux Criticité**
- **Critical** : Exploitation facile, impact élevé
- **High** : Exploitation possible, impact significatif  
- **Medium** : Conditions spécifiques requises
- **Low** : Impact limité

---

### 🔒 **Trivy**

Scanner de sécurité pour containers et dépendances.

**🐳 Scan Images Docker**
```bash
# Scan de l'image todo-app
trivy image todo-app:latest

# Résultat exemple
┌────────────┬──────────────┬──────────┐
│  Library   │    CVE-ID    │ Severity │
├────────────┼──────────────┼──────────┤
│ openssl    │ CVE-2024-123 │ HIGH     │
│ libssl3    │ CVE-2024-456 │ CRITICAL │
└────────────┴──────────────┴──────────┘
```

**📦 Types Scans**
- **OS Packages** : Vulnérabilités système
- **Language Dependencies** : Maven, npm, pip
- **Config Issues** : Dockerfile best practices
- **Secrets** : Clés, tokens dans code

---

## 📈 **Synergie des Technologies**

### 🎯 **Comment tout s'articule**

```
Client HTTP → Spring Boot App
    ├─ Spring Security (JWT)
    ├─ Spring Cache (Redis)  
    └─ Spring Data JPA (PostgreSQL)
    
Tests
    ├─ JUnit 5 (Framework)
    ├─ Testcontainers (PostgreSQL réel)
    ├─ ArchUnit (Validation DDD)
    └─ AssertJ (Assertions fluides)
    
Monitoring
    ├─ Micrometer → Prometheus → Grafana
    └─ Sleuth → Zipkin (Tracing)
    
CI/CD
    ├─ Jenkins (Pipeline)
    ├─ SonarQube (Qualité)
    ├─ OWASP (Sécurité deps)
    └─ Trivy (Scan containers)
```

Cette architecture garantit une application **robuste**, **sécurisée**, **observable** et **maintenable** suivant les meilleures pratiques du développement moderne.

## 📚 Ressources & Documentation

### Concepts DDD Expliqués

#### **Ubiquitous Language**
Vocabulaire partagé entre développeurs et experts métier :
- **Todo** : Tâche à accomplir
- **Complete** : Marquer comme terminé
- **Overdue** : En retard par rapport à l'échéance
- **Priority** : Niveau d'importance (Critical, High, Normal, Low)

#### **Bounded Context**
Le contexte "Todo Management" est délimité et autonome :
- Gestion complète du cycle de vie des todos
- Règles métier encapsulées
- Communication via événements

#### **Event Sourcing (partiel)**
Les événements du domaine tracent les changements :
```java
TodoCreatedEvent → TodoStartedEvent → TodoCompletedEvent
```

### Architecture Decision Records (ADR)

1. **ADR-001** : Choix de PostgreSQL vs MongoDB
2. **ADR-002** : Cache Redis pour performances
3. **ADR-003** : JWT pour authentification stateless
4. **ADR-004** : Testcontainers pour tests d'intégration

### Liens Utiles

- **DDD Reference** : https://domainlanguage.com/ddd/reference/
- **Spring Boot Docs** : https://spring.io/projects/spring-boot
- **ArchUnit Guide** : https://www.archunit.org/userguide/html/000_Index.html

## 🏆 Métriques de Qualité

### Code Quality

- **Maintainability Rating** : A
- **Reliability Rating** : A  
- **Security Rating** : A
- **Coverage** : >80%
- **Duplication** : <3%

### Performance Benchmarks

- **Startup Time** : <30s
- **Memory Usage** : <512MB
- **Response Time** : <200ms (p95)
- **Throughput** : >1000 req/s

---

## 📄 Licence

Ce projet est sous licence MIT. Voir [LICENSE](LICENSE) pour détails.

---

## 🎯 Conclusion

Cette application **Todo DDD** démontre l'implémentation professionnelle d'une architecture Domain-Driven Design avec Spring Boot moderne. Elle sert de référence complète pour :

- ✅ **Architecture DDD** complète et bien structurée
- ✅ **Tests exhaustifs** avec couverture >80%
- ✅ **Pipeline CI/CD** professionnel avec quality gates
- ✅ **Monitoring** et observabilité complets
- ✅ **Sécurité** intégrée à tous les niveaux
- ✅ **Documentation** complète et à jour

*Parfait pour apprendre, enseigner ou utiliser comme base pour vos projets DDD.*
