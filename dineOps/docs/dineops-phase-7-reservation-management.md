# DineOps Phase 7 — Reservation Management

## Purpose

This document describes the Phase 7 implementation for reservation management in DineOps, including entity design, APIs, validation rules, and branch-scoped access control.

---

## 1. Scope Covered

Phase 7 includes:

- Reservation domain model
- Reservation status lifecycle
- Reservation CRUD-like operational APIs (create/list/approve/reject)
- Reservation time and guest-count validations
- Branch-scoped visibility and access checks

---

## 2. Reservation Status Enum

Path: `src/main/java/com/mouad/dineops/dineOps/common/enums/ReservationStatus.java`

Implemented values:

- `PENDING`
- `APPROVED`
- `REJECTED`
- `COMPLETED`
- `CANCELED`

Current workflow operations in this phase:

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`

---

## 3. Domain Model

## Entity: `Reservation`

Path: `src/main/java/com/mouad/dineops/dineOps/reservation/entity/Reservation.java`

Key fields:

- `branch` (many-to-one)
- `customerName`
- `customerPhone`
- `customerEmail`
- `reservationTime`
- `numberOfGuests`
- `status`
- `notes`
- `createdAt`, `updatedAt`

Table name:

- `reservations`

---

## 4. Repository Layer

## Repository: `ReservationRepository`

Path: `src/main/java/com/mouad/dineops/dineOps/reservation/repository/ReservationRepository.java`

Implemented query methods:

- `findByBranchIdOrderByReservationTimeAsc(...)`
- `findByBranchIdAndStatusOrderByReservationTimeAsc(...)`

These support branch listing with optional status filtering.

---

## 5. DTOs

Paths under `src/main/java/com/mouad/dineops/dineOps/reservation/dto/`:

- `CreateReservationRequest`
- `ReservationResponse`

### `CreateReservationRequest`

Includes:

- `branchId`
- `customerName`
- `customerPhone`
- `customerEmail` (optional)
- `reservationTime`
- `numberOfGuests`
- `notes` (optional)

Validation annotations include:

- required fields (`@NotNull`, `@NotBlank`)
- email format (`@Email`)
- guest minimum (`@Min(1)`)
- max lengths for strings

---

## 6. Service Logic

## Service: `ReservationService`

Path: `src/main/java/com/mouad/dineops/dineOps/reservation/service/ReservationService.java`

Responsibilities:

- create reservation (`PENDING` by default)
- list reservations by branch (with optional status filter)
- approve reservation
- reject reservation
- enforce branch-scoped visibility
- apply time and guest validations

### Branch Scope Enforcement

Branch-scoped roles are restricted to assigned branches:

- `BRANCH_MANAGER`
- `CASHIER`
- `KITCHEN_STAFF`

Check used:

- `staffAssignmentRepository.existsByUserIdAndBranchIdAndActiveTrue(...)`

### Time Validation

Implemented checks:

- reservation time must be provided
- reservation time must be in the future
- when branch opening/closing times are configured, reservation time must fall within operating hours
- supports overnight operating windows (opening after closing)

### Guest Validation

Implemented check:

- `numberOfGuests > 0`

### Status Transition Guard

For this phase operations, only pending reservations can be approved or rejected.

---

## 7. Controller and Endpoints

## Controller: `ReservationController`

Path: `src/main/java/com/mouad/dineops/dineOps/reservation/controller/ReservationController.java`

Base path: `/api/reservations`

Endpoints:

- `POST /api/reservations`
  - create reservation
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`, `CASHIER`
- `GET /api/reservations?branchId={id}&status={optional}`
  - list reservations by branch and optional status
- `PATCH /api/reservations/{reservationId}/approve`
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`
- `PATCH /api/reservations/{reservationId}/reject`
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`

All endpoints return the standard `ApiResponse<T>` envelope.

---

## 8. Error Behavior

Common outcomes:

- `400 Bad Request`
  - invalid reservation time
  - invalid guest count
  - invalid status transition
  - inactive branch reservation action
- `403 Forbidden`
  - branch-scoped user attempts access outside assigned branch
- `404 Not Found`
  - missing branch or reservation

---

## 9. Verification Status

Phase 7 was validated with compile + local API smoke tests:

- create reservation
- list reservations
- approve reservation
- reject reservation
- guest-count validation
- status filter behavior

Result: reservation flow works with branch control.
