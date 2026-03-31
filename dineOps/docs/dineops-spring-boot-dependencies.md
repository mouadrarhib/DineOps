# DineOps — Spring Boot Dependencies
## Recommended dependency stack for the Multi-Branch Restaurant Operations Backend

This document lists the main dependencies you should use in your Spring Boot project for DineOps.

It is designed for a backend with:
- authentication and authorization
- role-based access
- PostgreSQL persistence
- Redis caching
- RabbitMQ asynchronous messaging
- API documentation
- testing
- monitoring
- Dockerized deployment

---

# 1. Core Spring Boot Dependencies

These are the main dependencies you should include from the beginning.

## Core API and Backend
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`

## Database
- `postgresql`
- `flyway-core`

## Caching
- `spring-boot-starter-data-redis`

## Messaging
- `spring-boot-starter-amqp`

## Documentation
- `springdoc-openapi-starter-webmvc-ui`

## Mapping and Boilerplate
- `mapstruct`
- `lombok`

## Testing
- `spring-boot-starter-test`
- `spring-security-test`
- `spring-boot-testcontainers`
- `testcontainers-junit-jupiter`
- `testcontainers-postgresql`

---

# 2. Why You Need Each Dependency

## `spring-boot-starter-web`
Use it to build:
- REST controllers
- JSON APIs
- request handling
- exception responses

## `spring-boot-starter-security`
Use it for:
- authentication
- authorization
- JWT integration
- securing endpoints

## `spring-boot-starter-data-jpa`
Use it for:
- JPA entities
- repositories
- database access
- transaction handling

## `spring-boot-starter-validation`
Use it for:
- request body validation
- DTO validation
- field rules such as `@NotNull`, `@Email`, `@Positive`

## `spring-boot-starter-actuator`
Use it for:
- health endpoints
- metrics
- observability
- production-style monitoring

## `postgresql`
Use it as the relational database driver.

## `flyway-core`
Use it for:
- database schema versioning
- migrations
- controlled database evolution

## `spring-boot-starter-data-redis`
Use it for:
- caching menu endpoints
- Redis integration
- performance improvement

## `spring-boot-starter-amqp`
Use it for:
- RabbitMQ integration
- asynchronous event processing
- notifications and background workflows

## `springdoc-openapi-starter-webmvc-ui`
Use it for:
- Swagger UI
- API documentation
- endpoint testing in browser

## `mapstruct`
Use it for:
- DTO to entity mapping
- entity to response mapping
- cleaner service code

## `lombok`
Use it to reduce boilerplate:
- getters
- setters
- constructors
- builders

## `spring-boot-starter-test`
Use it for:
- unit tests
- integration tests
- Spring testing support

## `spring-security-test`
Use it for:
- testing secured endpoints
- testing authentication and roles

## `spring-boot-testcontainers`
Use it for:
- easier Spring Boot integration with Testcontainers

## `testcontainers-junit-jupiter`
Use it for:
- JUnit 5 support with containers

## `testcontainers-postgresql`
Use it for:
- PostgreSQL integration tests with real containerized database

---

# 3. Recommended Dependency Groups by Feature

## Essential MVP Dependencies
These are enough to build the first strong version:
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `postgresql`
- `flyway-core`
- `springdoc-openapi-starter-webmvc-ui`
- `lombok`
- `mapstruct`
- `spring-boot-starter-test`
- `spring-security-test`

## Add for Phase 2
- `spring-boot-starter-data-redis`
- `spring-boot-starter-actuator`

## Add for Phase 3
- `spring-boot-starter-amqp`
- `spring-boot-testcontainers`
- `testcontainers-junit-jupiter`
- `testcontainers-postgresql`

---

# 4. Ready-to-Paste Maven Dependencies Block

```xml
<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Messaging -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>

    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>REPLACE_WITH_CURRENT_VERSION</version>
    </dependency>

    <!-- Mapping -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>REPLACE_WITH_CURRENT_VERSION</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

# 5. Optional Dependencies You May Add Later

These are useful, but not mandatory at the beginning.

## Mail
If you want real email sending:
- `spring-boot-starter-mail`

## Dev Convenience
For automatic restart in development:
- `spring-boot-devtools`

## Metrics Export
If needed depending on setup:
- Micrometer Prometheus registry

Example:
- `micrometer-registry-prometheus`

---

# 6. Dependencies I Do Not Recommend at the Beginning

To avoid unnecessary complexity, do not start with these unless you truly need them:
- `spring-boot-starter-webflux`
- Kafka dependencies
- Elasticsearch
- Spring Cloud
- QueryDSL
- Liquibase together with Flyway

---

# 7. Best Final Dependency Recommendation

For DineOps, your best dependency stack is:

## Main stack
- Web
- Security
- Data JPA
- Validation
- Actuator
- PostgreSQL
- Flyway
- Redis
- RabbitMQ
- Swagger/OpenAPI
- MapStruct
- Lombok
- Testing
- Testcontainers

This stack is strong enough to support:
- JWT authentication
- RBAC
- branch-scoped authorization
- transactional order workflow
- inventory deduction
- caching
- async processing
- monitoring
- real integration tests

---

# 8. Final Note

If you want the cleanest setup, start with the **essential MVP dependencies first**, then add:
- Redis
- RabbitMQ
- Actuator
- Testcontainers

step by step as you implement advanced features.

That will keep the project manageable while still making it look like a serious backend system.
