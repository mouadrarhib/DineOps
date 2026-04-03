# DineOps Redis Caching Guide

## Purpose

This guide explains how Redis is used in DineOps for caching, how to run and access Redis locally, how to enable/disable cache modes, and how to verify cached behavior.

---

## 1. Redis in DineOps

Redis is used as a cache backend for read-heavy endpoints.

Current cache use cases:

- menu categories by branch
- menu items by branch
- branch details by id

Cache integration is implemented through Spring Cache in:

- `src/main/java/com/mouad/dineops/dineOps/common/config/CacheConfig.java`

---

## 2. Cache Modes

DineOps supports three cache modes:

- **Redis cache mode**
  - `app.cache.enabled=true`
  - `app.cache.redis-enabled=true`
- **In-memory cache mode (local fallback)**
  - `app.cache.enabled=true`
  - `app.cache.redis-enabled=false`
- **No cache mode**
  - `app.cache.enabled=false`

This lets you benchmark and troubleshoot cache behavior without changing code.

---

## 3. Configuration Keys

Main keys in `src/main/resources/application.yml`:

- `spring.data.redis.host`
- `spring.data.redis.port`
- `spring.data.redis.timeout`
- `app.cache.enabled`
- `app.cache.redis-enabled`
- `app.cache.ttl.menu-categories-minutes`
- `app.cache.ttl.menu-items-minutes`
- `app.cache.ttl.branch-details-minutes`

Environment override examples:

- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_TIMEOUT`
- `APP_CACHE_ENABLED`
- `APP_CACHE_REDIS_ENABLED`
- `APP_CACHE_TTL_MENU_CATEGORIES_MINUTES`
- `APP_CACHE_TTL_MENU_ITEMS_MINUTES`
- `APP_CACHE_TTL_BRANCH_DETAILS_MINUTES`

---

## 4. Run Redis Locally

## Option A: Docker

```bash
docker run --name dineops-redis -p 6379:6379 -d redis:7
```

Check container:

```bash
docker ps
```

Stop/remove:

```bash
docker stop dineops-redis
docker rm dineops-redis
```

## Option B: Existing local Redis service

If Redis is already installed locally, ensure it is running on the configured host/port.

---

## 5. Enable Redis Caching in App

Example (PowerShell):

```powershell
$env:APP_CACHE_ENABLED="true"
$env:APP_CACHE_REDIS_ENABLED="true"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Example (CMD):

```bat
set APP_CACHE_ENABLED=true
set APP_CACHE_REDIS_ENABLED=true
set REDIS_HOST=localhost
set REDIS_PORT=6379
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 6. How to Access Redis Directly

## Redis CLI from host

```bash
redis-cli -h localhost -p 6379
```

Useful commands:

```text
PING
KEYS *
SCAN 0
TTL <key>
GET <key>
DEL <key>
FLUSHDB
```

Notes:

- prefer `SCAN` over `KEYS *` on large datasets
- many Spring cache values are binary/serialized payloads, so raw `GET` may not be human-friendly

## Redis CLI from Docker container

```bash
docker exec -it dineops-redis redis-cli
```

---

## 7. Cache Names and What They Store

Configured cache names:

- `menuCategoriesByBranch`
- `menuItemsByBranch`
- `branchDetails`

Key composition is driven by service-level `@Cacheable` expressions.

---

## 8. Cache Eviction Rules

To avoid stale data, DineOps evicts cache entries when data changes.

Current eviction behavior:

- category create/update/activate/deactivate
  - evicts `menuCategoriesByBranch`
  - evicts `menuItemsByBranch`
- menu item create/update/availability toggle
  - evicts `menuItemsByBranch`
- branch create/update/activate/deactivate
  - evicts `branchDetails`

---

## 9. How to Validate Caching Works

Basic validation flow:

1. start app with cache enabled
2. call a cached endpoint once (cold read)
3. repeat same request multiple times (warm reads)
4. compare response times
5. change related data (create/update endpoint)
6. call read endpoint again and verify freshness after eviction

Recommended endpoints to test:

- `GET /api/menu-categories?branchId=...&search=&page=0&size=20`
- `GET /api/menu-items?branchId=...&search=&page=0&size=20`
- `GET /api/branches/{branchId}`

---

## 10. Troubleshooting

## App starts but caching seems inactive

- check `APP_CACHE_ENABLED` and `APP_CACHE_REDIS_ENABLED`
- confirm Redis host/port values
- verify repeated requests are identical (same query params)

## Redis connection errors

- ensure Redis server/container is running
- verify port exposure (`6379` by default)
- check firewall/network constraints

## Stale data observed

- verify write path has matching `@CacheEvict`
- ensure writes and reads use the same cache keys/parameters

---

## 11. Security and Production Notes

- avoid exposing Redis publicly without auth/network controls
- in production, configure Redis auth/TLS as required by infrastructure
- avoid using `FLUSHDB` in shared or production environments
