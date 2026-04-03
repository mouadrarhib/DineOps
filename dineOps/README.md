# DineOps Backend

## Project vision and architecture

### Vision

DineOps is a backend platform for multi-branch restaurant operations. The goal is to provide one consistent system for authentication, branch-scoped operations, menu and order flows, inventory tracking, and operational reporting.

### Architecture direction

- Domain-driven package structure by business module (`auth`, `user`, `restaurant`, `branch`, `menu`, `order`, `inventory`, `reservation`, `notification`, `reporting`, `audit`).
- Shared cross-cutting layer in `common` for configuration, security, exception handling, API response format, enums, and utilities.
- REST API first with Spring Boot + Spring Security + validation.
- PostgreSQL as primary transactional store with Flyway migrations for schema evolution.
- Redis for caching and RabbitMQ for asynchronous workflows.
- Environment-based profiles (`local`, `test`, `prod`) for predictable behavior across development, test, and production.
- Docker and Docker Compose support for reproducible local infrastructure and deployment parity.

### Current implementation baseline

- Global security configuration with public health endpoint.
- Unified API response envelope and base exception handling (`@RestControllerAdvice`).
- Containerized runtime setup for app, PostgreSQL, Redis, and RabbitMQ.

## Coding conventions

- Use package-by-feature for business modules (`auth`, `user`, `restaurant`, `branch`, `menu`, `order`, `inventory`, `reservation`, `notification`, `reporting`, `audit`).
- Keep shared cross-cutting code only in `common` (`config`, `exception`, `response`, `security`, `util`, `enums`).
- Name REST endpoints with plural, resource-based paths and keep controller methods thin.
- Return `ApiResponse<T>` for successful and error responses to keep response shape consistent.
- Throw domain-focused custom exceptions (`NotFoundException`, `BadRequestException`, `ConflictException`, etc.) and let global handlers format responses.
- Extend `BaseEntity` for persistence models to get `id`, `createdAt`, and `updatedAt` automatically.
- Keep validation in request DTOs and let global validation error handlers build standardized error payloads.
- Keep config externalized by profile (`local`, `test`, `prod`) and avoid hardcoded credentials.
- For migrations, add Flyway scripts under `src/main/resources/db/migration` using `V{number}__{description}.sql` naming.

## Environment variables (Windows)

This project reads database credentials from environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

You can copy `.env.example` as reference values.

### PowerShell (current terminal session)

```powershell
$env:DB_URL="jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="your_db_username"
$env:DB_PASSWORD="your_db_password"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.10"
mvn spring-boot:run
```

### CMD (current terminal session)

```bat
set DB_URL=jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:5432/postgres?sslmode=require
set DB_USERNAME=your_db_username
set DB_PASSWORD=your_db_password
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
mvn spring-boot:run
```

### Persist variables for your user (PowerShell)

```powershell
[Environment]::SetEnvironmentVariable("DB_URL", "jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:5432/postgres?sslmode=require", "User")
[Environment]::SetEnvironmentVariable("DB_USERNAME", "your_db_username", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_db_password", "User")
```

After persisting them, restart your terminal before running Maven.

## Run with profiles

Available profiles:

- `local`
- `test`
- `prod`

### PowerShell commands

Run with `local` profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Run with `test` profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=test"
```

Run with `prod` profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
```

Notes:

- `local` uses local-friendly defaults and supports `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` overrides.
- `test` uses `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD` and disables Flyway.
- `prod` requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Caching strategy (Phase 9)

Read-heavy endpoints are cached with Spring Cache using a Redis-ready cache manager.

### Cached endpoints

- menu categories by branch (paginated/filter key)
- menu items by branch (paginated/filter key)
- branch details by branch id

### Cache names and TTL

- `menuCategoriesByBranch` - default 10 minutes
- `menuItemsByBranch` - default 5 minutes
- `branchDetails` - default 30 minutes

### Cache backend behavior

- `app.cache.enabled=true` enables caching
- `app.cache.redis-enabled=true` uses Redis cache manager
- `app.cache.redis-enabled=false` uses in-memory cache manager (local fallback)
- `app.cache.enabled=false` disables caching (no-op manager)

Relevant config keys in `application.yml`:

- `spring.data.redis.host`
- `spring.data.redis.port`
- `spring.data.redis.timeout`
- `app.cache.*`

### Cache eviction policy

To keep cached reads consistent, menu cache entries are evicted on menu/category writes:

- category create/update/activate/deactivate evicts category and item caches
- menu item create/update/availability toggle evicts item cache

Branch detail cache is evicted on branch create/update/activate/deactivate.

### Response-time benchmark (local)

Measured using repeated authenticated requests to read-heavy endpoints.

- caching OFF (avg ms)
  - menu categories: `272.79`
  - menu items: `558.68`
  - branch details: `199.22`
- caching ON (avg ms)
  - menu categories: `99.54`
  - menu items: `86.44`
  - branch details: `90.70`

This shows a clear performance improvement story for repeated reads.

Detailed Redis usage and access guide:

- `docs/dineops-redis-caching-guide.md`
