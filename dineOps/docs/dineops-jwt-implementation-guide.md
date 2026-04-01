# DineOps JWT Implementation Guide

## Purpose

This document describes how JWT authentication is implemented in the DineOps backend, including the class responsibilities, request flow, token lifecycle, and error handling behavior.

---

## 1. Implemented Authentication Endpoints

Base path: `/api/auth`

- `POST /login` - authenticate user and issue tokens
- `POST /refresh` - rotate refresh token and issue new access token
- `POST /logout` - revoke refresh token
- `GET /me` - return current authenticated user details
- `POST /seed` - create virtual roles/permissions/users for local testing

Response format for all endpoints uses `ApiResponse<T>`.

---

## 2. Key Classes and Responsibilities

## Controller Layer

### `AuthController`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/controller/AuthController.java`

Responsibilities:
- exposes auth APIs (`login`, `refresh`, `logout`, `me`, `seed`)
- validates request DTOs
- delegates logic to `AuthService`

## Service Layer

### `AuthService`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/service/AuthService.java`

Responsibilities:
- authenticate credentials via `AuthenticationManager`
- generate access and refresh tokens
- persist refresh tokens in DB
- rotate refresh tokens on refresh
- revoke refresh token on logout
- build current-user response payload
- seed virtual auth data (roles, permissions, users)

### `PasswordHashService`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/security/PasswordHashService.java`

Responsibilities:
- hash passwords using BCrypt
- verify raw password against hash

## JWT and Security Layer

### `JwtTokenService`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/security/JwtTokenService.java`

Responsibilities:
- generate JWT access token (`type=access`)
- generate JWT refresh token (`type=refresh`)
- validate token signature, type, and expiration
- extract subject and expiration

### `JwtAuthenticationFilter`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/security/JwtAuthenticationFilter.java`

Responsibilities:
- intercept incoming requests
- extract `Authorization: Bearer <token>`
- validate access token
- load user details and set `SecurityContext`

### `JwtAuthenticationEntryPoint`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/security/JwtAuthenticationEntryPoint.java`

Responsibilities:
- return standardized `401 Unauthorized` response when authentication fails at filter/security level

### `CustomUserDetailsService`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/security/CustomUserDetailsService.java`

Responsibilities:
- load user by email
- fetch user roles and permissions
- map them to Spring Security authorities
- reject inactive users

### `AppUserPrincipal`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/security/AppUserPrincipal.java`

Responsibilities:
- custom authenticated principal object
- carries user identity + status + roles + permissions

## Persistence Layer

### `RefreshToken`
Path: `src/main/java/com/mouad/dineops/dineOps/auth/entity/RefreshToken.java`

Stored fields:
- `user`
- `token`
- `expiresAt`
- `revoked`

### Repositories

- `UserRepository` (with `findWithRolesAndPermissionsByEmail`)
- `RoleRepository`
- `PermissionRepository`
- `RefreshTokenRepository`

---

## 3. DTOs Used in Auth Flow

Paths under `src/main/java/com/mouad/dineops/dineOps/auth/dto/`

- `LoginRequest`
- `RefreshTokenRequest`
- `LogoutRequest`
- `AuthTokensResponse`
- `CurrentUserResponse`
- `SeedDataResponse`

---

## 4. Token Lifecycle

## Access Token
- short-lived
- used to access protected APIs
- sent via `Authorization` header

## Refresh Token
- longer-lived
- stored in DB
- used only on `/api/auth/refresh`
- rotated on each refresh
- revoked on logout

## Revocation Strategy
- on logout, token is marked revoked
- on refresh, old refresh token is revoked and replaced
- revoked token cannot be used again

---

## 5. Security Configuration Notes

Main configuration class:
- `src/main/java/com/mouad/dineops/dineOps/common/config/SecurityConfig.java`

Important points:
- stateless session policy
- JWT filter registered before username/password auth filter
- DAO auth provider + BCrypt password encoder
- public endpoints include `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`, `/api/auth/seed`
- all other endpoints require authentication by default

---

## 6. JWT Configuration Properties

Defined in `application.yml`:

- `app.jwt.secret`
- `app.jwt.access-token-expiration-minutes`
- `app.jwt.refresh-token-expiration-days`

These should be externalized in production using environment variables.

---

## 7. Exception and Validation Behavior

Handled by `GlobalExceptionHandler`:

- invalid credentials -> `401`
- invalid refresh token -> `401`
- validation errors -> `400` with field-level error list
- unhandled exceptions -> `500`

All responses use consistent API envelope.

---

## 8. Verified API Scenarios

The following were tested successfully:

- seed virtual auth data
- login with valid credentials
- login with wrong password
- login with validation errors
- get current user from access token
- refresh tokens
- logout
- reject refresh after logout
