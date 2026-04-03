# DineOps Phase 10 — Audit Logging and Reporting

## Purpose

This document describes the Phase 10 implementation that adds traceability and analytics value to the DineOps backend.

---

## 1. Scope Covered

Phase 10 includes:

- audit log domain and service
- sensitive-action audit capture
- reporting APIs for operations and management
- date-range filtering for reports

---

## 2. Audit Logging (10.1)

## Entity

Path: `src/main/java/com/mouad/dineops/dineOps/audit/entity/AuditLog.java`

Table: `audit_logs`

Fields:

- `actorUserId`
- `actorEmail`
- `action`
- `entityType`
- `entityId`
- `branchId`
- `details`
- `createdAt`, `updatedAt`

## Repository

Path: `src/main/java/com/mouad/dineops/dineOps/audit/repository/AuditLogRepository.java`

Provides persistence for audit entries.

## Service

Path: `src/main/java/com/mouad/dineops/dineOps/audit/service/AuditLogService.java`

Responsibilities:

- create audit entries consistently
- resolve actor context from authenticated principal when available
- record action metadata (`action`, `entityType`, ids, details)

### Sensitive Actions Tracked

Implemented capture points:

- role assignment
  - `src/main/java/com/mouad/dineops/dineOps/staff/service/StaffAssignmentService.java`
  - action: `ROLE_ASSIGNMENT`
- menu updates
  - `src/main/java/com/mouad/dineops/dineOps/menu/service/MenuItemService.java`
  - `src/main/java/com/mouad/dineops/dineOps/menu/service/MenuCategoryService.java`
  - action: `MENU_UPDATED`
- inventory restocks
  - `src/main/java/com/mouad/dineops/dineOps/inventory/service/InventoryItemService.java`
  - action: `INVENTORY_RESTOCK`
- order cancellations
  - `src/main/java/com/mouad/dineops/dineOps/order/service/OrderService.java`
  - action: `ORDER_CANCELED`
- branch updates
  - `src/main/java/com/mouad/dineops/dineOps/branch/service/BranchService.java`
  - action: `BRANCH_UPDATED`

---

## 3. Reporting (10.2)

## Controller

Path: `src/main/java/com/mouad/dineops/dineOps/reporting/controller/ReportingController.java`

Base path: `/api/reports`

Endpoints:

- `GET /api/reports/daily-sales`
  - params: `fromDate`, `toDate`, optional `branchId`
  - returns aggregated daily totals by date+branch
- `GET /api/reports/order-summary`
  - params: `fromDate`, `toDate`, optional `branchId`
  - returns status distribution + revenue summary
- `GET /api/reports/branch-performance`
  - params: `fromDate`, `toDate`
  - returns branch-level operational performance

Role restrictions:

- daily-sales, order-summary: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`
- branch-performance: `SUPER_ADMIN`, `RESTAURANT_OWNER`

## Service

Path: `src/main/java/com/mouad/dineops/dineOps/reporting/service/ReportingService.java`

Implemented behavior:

- validates date range (`fromDate`, `toDate` required, `toDate >= fromDate`)
- aggregates totals by date and branch for daily sales
- calculates order lifecycle counters for summary report
- computes branch performance ranking by completed revenue

## DTOs

Paths:

- `src/main/java/com/mouad/dineops/dineOps/reporting/dto/DailySalesReportRow.java`
- `src/main/java/com/mouad/dineops/dineOps/reporting/dto/OrderSummaryReportResponse.java`
- `src/main/java/com/mouad/dineops/dineOps/reporting/dto/BranchPerformanceRow.java`
- `src/main/java/com/mouad/dineops/dineOps/reporting/dto/BranchPerformanceReportResponse.java`

## Repository Support for Date Filtering

Updated:

- `src/main/java/com/mouad/dineops/dineOps/order/repository/CustomerOrderRepository.java`

Added query methods supporting:

- created-at range filtering
- completed-at range filtering
- optional branch scoping

---

## 4. Aggregation Model

Implemented aggregation dimensions:

- by `date` and `branch` (daily sales)
- by order `status` (order summary)
- by `branch` totals (branch performance)

Primary monetary metrics:

- subtotal
- tax
- total revenue
- average completed order value

---

## 5. Date Range Behavior

All report endpoints support explicit date filtering.

Rules:

- `fromDate` and `toDate` are mandatory
- range is inclusive by day semantics (`toDate` converted to next-day start for upper bound)
- UTC conversion is used for report window boundaries

---

## 6. Verification Status

Phase 10 was validated with compile + local smoke tests:

- sensitive actions executed and persisted with audit integration
- reporting endpoints returned aggregated data successfully
- date range filters exercised through API calls

Result: backend now includes administration-grade traceability and analytics/reporting value.
