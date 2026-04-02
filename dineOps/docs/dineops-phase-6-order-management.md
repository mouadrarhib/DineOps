# DineOps Phase 6 — Order Management

## Purpose

This document describes the Phase 6 implementation of the operational order workflow in DineOps, including order entities, APIs, pricing calculations, stock deduction on confirmation, and workflow authorization/transition rules.

---

## 1. Scope Covered

Phase 6 includes:

- Order entities (`CustomerOrder`, `OrderItem`, `OrderStatus`)
- Order management APIs
- Order financial calculations (subtotal, tax, total)
- Inventory deduction on order confirmation
- Workflow rules (transition guards + role restrictions)

---

## 2. Domain Model

## `OrderStatus` Enum

Path: `src/main/java/com/mouad/dineops/dineOps/common/enums/OrderStatus.java`

Values:

- `PENDING`
- `CONFIRMED`
- `IN_PREPARATION`
- `READY`
- `COMPLETED`
- `CANCELED`

## `CustomerOrder` Entity

Path: `src/main/java/com/mouad/dineops/dineOps/order/entity/CustomerOrder.java`

Key fields:

- `branch`
- `orderNumber` (unique)
- `status`
- `source` (`IN_STORE`, `PHONE`, `ONLINE`)
- `subtotal`
- `taxAmount`
- `totalAmount`
- `notes`
- `createdBy`
- `confirmedAt`
- `completedAt`
- `createdAt`, `updatedAt`

## `OrderItem` Entity

Path: `src/main/java/com/mouad/dineops/dineOps/order/entity/OrderItem.java`

Key fields:

- `order`
- `menuItem`
- `quantity`
- `unitPrice` (price snapshot)
- `totalPrice` (line total snapshot)
- `notes`
- `createdAt`, `updatedAt`

---

## 3. Repositories

## `CustomerOrderRepository`

Path: `src/main/java/com/mouad/dineops/dineOps/order/repository/CustomerOrderRepository.java`

Key operations:

- `existsByOrderNumber(...)`
- `findByOrderNumber(...)`
- `findByBranchIdOrderByCreatedAtDesc(...)`
- `findByBranchIdAndStatusOrderByCreatedAtDesc(...)`

## `OrderItemRepository`

Path: `src/main/java/com/mouad/dineops/dineOps/order/repository/OrderItemRepository.java`

Key operation:

- `findByOrderIdOrderByIdAsc(...)`

---

## 4. API Layer

## Controller

Path: `src/main/java/com/mouad/dineops/dineOps/order/controller/OrderController.java`

Base path: `/api/orders`

Endpoints:

- `POST /api/orders`
  - create order
- `GET /api/orders?branchId={id}&status={optional}`
  - list branch orders
- `GET /api/orders/{orderId}`
  - get order details
- `PATCH /api/orders/{orderId}/confirm`
  - confirm order (deduct stock)
- `PATCH /api/orders/{orderId}/start-preparation`
  - move to `IN_PREPARATION`
- `PATCH /api/orders/{orderId}/ready`
  - move to `READY`
- `PATCH /api/orders/{orderId}/complete`
  - move to `COMPLETED`
- `PATCH /api/orders/{orderId}/cancel`
  - cancel order (when transition allowed)

All responses use `ApiResponse<T>`.

---

## 5. Service Rules

## Core Service

Path: `src/main/java/com/mouad/dineops/dineOps/order/service/OrderService.java`

Responsibilities:

- create order from validated request items
- persist order and order items atomically
- enforce branch-scope restrictions for branch-scoped users
- enforce role checks for creation and kitchen workflow methods
- enforce valid status transitions
- deduct inventory at confirmation time with transaction safety
- write inventory movement history entries for deductions

---

## 6. Order Calculations (6.3)

## Request Inputs

Path: `src/main/java/com/mouad/dineops/dineOps/order/dto/CreateOrderRequest.java`

Pricing inputs:

- `taxRatePercent` (optional)
- `taxAmount` (optional fallback)

## Calculation Rules

- `subtotal = sum(orderItem.unitPrice * quantity)`
- if `taxRatePercent` provided:
  - `taxAmount = subtotal * taxRatePercent / 100`
- else:
  - `taxAmount` uses validated explicit tax amount (or `0`)
- `totalAmount = subtotal + taxAmount`
- money values are normalized to 2 decimals (`HALF_UP`)

## Pricing Snapshot

At order creation time, each order item stores:

- `unitPrice` copied from current `menuItem.price`
- `totalPrice` calculated for that line

This preserves pricing history even if menu prices change later.

---

## 7. Inventory Deduction on Confirmation (6.4)

## Confirmation Validation

Before moving `PENDING -> CONFIRMED`, service validates:

- ordered menu items are still active and available
- all order items belong to the same branch as the order
- ingredient mappings (`menu_item_ingredients`) are valid
- inventory exists for required branch+ingredient pairs
- available stock is sufficient for required ingredient quantities

If any check fails, confirmation is blocked with `400 Bad Request`.

## Deduction Behavior

- required ingredient quantities are aggregated across all order items
- inventory is deducted per ingredient
- deduction and status update happen inside one transaction
- insufficient stock prevents status change and prevents partial deduction

## Movement History

Implemented movement tracking:

- enum: `src/main/java/com/mouad/dineops/dineOps/common/enums/InventoryMovementType.java`
- entity: `src/main/java/com/mouad/dineops/dineOps/inventory/entity/InventoryMovement.java`
- repository: `src/main/java/com/mouad/dineops/dineOps/inventory/repository/InventoryMovementRepository.java`

On confirmation, a `DEDUCTION` movement is recorded per affected ingredient with:

- branch
- ingredient
- quantity changed (negative)
- reference type/id (`CUSTOMER_ORDER`, order id)
- actor (`createdBy`)
- notes

---

## 8. Workflow Rules (6.5)

## Allowed Status Transitions

- `PENDING -> CONFIRMED, CANCELED`
- `CONFIRMED -> IN_PREPARATION, CANCELED`
- `IN_PREPARATION -> READY, CANCELED`
- `READY -> COMPLETED, CANCELED`
- `COMPLETED -> (none)`
- `CANCELED -> (none)`

Invalid transitions are rejected.

## Role Restrictions

- order creation is restricted to cashier/authorized roles
- kitchen workflow endpoints (`start-preparation`, `ready`) are restricted to:
  - `KITCHEN_STAFF`
  - `BRANCH_MANAGER`

Branch-scoped users are additionally limited to their assigned branch.

---

## 9. Error Behavior

Common outcomes:

- `400 Bad Request`
  - invalid status transition
  - invalid pricing input
  - unavailable menu item
  - insufficient inventory
- `403 Forbidden`
  - role not allowed for action
  - branch-scoped access violation
- `404 Not Found`
  - missing branch/menu item/order/user

---

## 10. Verification Status

Phase 6 functionality was verified with compile + local API smoke tests:

- create/list/details order endpoints
- full workflow transitions
- invalid transition blocking
- stock deduction on confirmation
- insufficient stock rejection
- kitchen workflow role checks
