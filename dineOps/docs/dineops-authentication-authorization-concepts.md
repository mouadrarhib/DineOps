# DineOps Authentication and Authorization Concepts

## Purpose

This document explains the core concepts of authentication and authorization in the DineOps backend and how they apply to a multi-branch restaurant system.

---

## 1. Authentication vs Authorization

## Authentication (Who are you?)

Authentication verifies identity.

In DineOps:
- user provides credentials (`email` + `password`)
- system validates credentials
- system issues JWT tokens after successful login

Result: user becomes an authenticated principal.

## Authorization (What can you do?)

Authorization decides allowed actions after authentication.

In DineOps:
- user has one or more roles (e.g., `BRANCH_MANAGER`)
- roles grant permissions (e.g., `MANAGE_INVENTORY`)
- secured endpoints check authentication and roles/permissions

Result: only allowed operations are executed.

---

## 2. Why This Matters for DineOps

DineOps is branch-scoped and role-driven. Different staff must have different access levels:

- `SUPER_ADMIN`: full system-level control
- `RESTAURANT_OWNER`: owner-level operational control
- `BRANCH_MANAGER`: manages a specific branch
- `CASHIER`: order/front-desk actions
- `KITCHEN_STAFF`: kitchen and stock-related operations

Without proper auth design, users could access cross-branch or sensitive operations incorrectly.

---

## 3. Domain Model for Access Control

## User
- real person account
- login identity and status (`ACTIVE`, `INACTIVE`, `SUSPENDED`)

## Role
- high-level business responsibility
- assigned to users (many-to-many)

## Permission
- fine-grained action capability
- assigned to roles (many-to-many)

This model enables RBAC (Role-Based Access Control) with optional permission granularity.

---

## 4. JWT-Based Session Strategy

## Access Token
- short lifespan
- used on each protected API call
- carries identity and role claims

## Refresh Token
- longer lifespan
- stored in DB
- used to obtain new access token without re-login

## Benefits
- stateless API authentication
- scalable for distributed environments
- explicit control with refresh token revocation

---

## 5. Security Principles Applied

- passwords are hashed with BCrypt (never stored as plain text)
- auth responses are consistent (`ApiResponse<T>`)
- invalid credentials return controlled errors
- refresh token revocation supports logout/invalidation
- token type checks prevent misuse (`access` vs `refresh`)
- user status can block inactive accounts

---

## 6. Typical Request Flow

1. Client calls `POST /api/auth/login`
2. Server authenticates and returns access + refresh tokens
3. Client calls protected endpoint with `Authorization: Bearer <access_token>`
4. JWT filter validates token and sets security context
5. Endpoint executes if user is authorized
6. When access token expires, client calls `POST /api/auth/refresh`
7. Server rotates refresh token and issues new token pair
8. On logout, client calls `POST /api/auth/logout` to revoke refresh token

---

## 7. Multi-Branch Authorization Direction

Current implementation provides role/permission foundations.

Next recommended enforcement layer:
- include branch context in token or request scope
- validate that branch-scoped roles only act within assigned branch
- enforce rules like:
  - cashiers cannot create orders in another branch
  - branch managers cannot manage other branches

---

## 8. Recommended Practices Going Forward

- keep role names stable and explicit
- keep permission names action-oriented (`UPDATE_MENU`, `VIEW_REPORTS`)
- centralize access checks in service/security layer
- write authorization tests per role and branch context
- seed minimum required roles/permissions in controlled migrations

---

## 9. Summary

Authentication in DineOps establishes user identity securely via JWT.
Authorization ensures that authenticated users can only perform actions allowed by their roles and permissions.

Together, they provide a secure, scalable, and auditable foundation for a real multi-branch operations backend.
