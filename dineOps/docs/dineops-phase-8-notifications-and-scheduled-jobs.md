# DineOps Phase 8 — Notifications and Scheduled Jobs

## Purpose

This document describes the Phase 8 implementation that introduces operational automation through notifications and scheduled jobs.

---

## 1. Scope Covered

Phase 8 includes:

- notification domain and APIs
- internal notification support
- email notification abstraction with demo sender
- scheduled jobs for low stock, reservation reminders, daily sales summary, and refresh-token cleanup
- business notification triggers for stock/reservation/sales events

---

## 2. Notification Domain (8.1)

## Enums

Paths:

- `src/main/java/com/mouad/dineops/dineOps/common/enums/NotificationType.java`
- `src/main/java/com/mouad/dineops/dineOps/common/enums/NotificationStatus.java`

Values:

- `NotificationType`: `EMAIL`, `IN_APP`
- `NotificationStatus`: `PENDING`, `SENT`, `FAILED`

## Entity

Path: `src/main/java/com/mouad/dineops/dineOps/notification/entity/Notification.java`

Table: `notifications`

Key fields:

- `type`
- `recipient`
- `subject`
- `content`
- `status`
- `relatedEntityType`
- `relatedEntityId`
- `sentAt`
- `createdAt`, `updatedAt`

## Repository

Path: `src/main/java/com/mouad/dineops/dineOps/notification/repository/NotificationRepository.java`

Key methods:

- `findByRecipientOrderByCreatedAtDesc(...)`
- `findByStatusOrderByCreatedAtAsc(...)`
- `existsByTypeAndRelatedEntityTypeAndRelatedEntityId(...)`

## Service

Path: `src/main/java/com/mouad/dineops/dineOps/notification/service/NotificationService.java`

Implemented capabilities:

- `sendInternal(...)` for internal/app notifications
- `sendEmail(...)` for email notifications (persist + send + update status)
- `createInternalNotification(...)` endpoint-oriented wrapper
- `sendTestEmail(...)` endpoint-oriented wrapper

## Email Abstraction

Paths:

- `src/main/java/com/mouad/dineops/dineOps/notification/service/EmailNotificationSender.java`
- `src/main/java/com/mouad/dineops/dineOps/notification/service/LoggingEmailNotificationSender.java`

Current implementation is a demo/log sender (safe for local development).

## Controller and Endpoints

Path: `src/main/java/com/mouad/dineops/dineOps/notification/controller/NotificationController.java`

Base path: `/api/notifications`

Endpoints:

- `POST /api/notifications/internal`
- `POST /api/notifications/test-email`

Request DTOs:

- `src/main/java/com/mouad/dineops/dineOps/notification/dto/InternalNotificationRequest.java`
- `src/main/java/com/mouad/dineops/dineOps/notification/dto/TestEmailRequest.java`

Response DTO:

- `src/main/java/com/mouad/dineops/dineOps/notification/dto/NotificationResponse.java`

---

## 3. Scheduled Jobs (8.2)

## Scheduling Enablement

Scheduling is enabled in:

- `src/main/java/com/mouad/dineops/dineOps/DineOpsApplication.java`

with `@EnableScheduling`.

## Job Runner

Path: `src/main/java/com/mouad/dineops/dineOps/notification/job/OperationsScheduledJobs.java`

Implemented jobs:

- **Low-stock job**
  - scans active branches
  - finds low-stock inventory items
  - sends manager alerts
- **Reservation reminder job**
  - finds approved reservations in the reminder window
  - sends reminder (email if customer email exists, otherwise internal)
  - deduplicates reminders via notification existence check
- **Daily sales summary job**
  - computes previous UTC day completed-order summary by branch
  - sends summary to owner channel (internal + email if restaurant email exists)
- **Refresh token cleanup job**
  - deletes expired tokens
  - deletes revoked+expired tokens

## Cron Configuration

Added in `src/main/resources/application.yml`:

- `app.jobs.low-stock-cron`
- `app.jobs.reservation-reminder-cron`
- `app.jobs.daily-sales-summary-cron`
- `app.jobs.refresh-token-cleanup-cron`

All are overrideable through environment variables.

---

## 4. Notification Triggers (8.3)

Implemented triggers:

- **Notify manager on low stock**
  - scheduled low-stock job notifies active branch managers
- **Notify on reservation approval**
  - in `ReservationService.approveReservation(...)`, sends:
    - internal approval notification
    - email notification when customer email exists
- **Notify before reservation time**
  - reservation reminder scheduled job
- **Notify owner with daily sales summary**
  - daily summary scheduled job (internal + optional email)

---

## 5. Supporting Repository Updates

Updated repositories to support Phase 8 automation:

- `src/main/java/com/mouad/dineops/dineOps/branch/repository/BranchRepository.java`
  - `findByStatus(...)`
- `src/main/java/com/mouad/dineops/dineOps/staff/repository/StaffAssignmentRepository.java`
  - manager lookup with joined user loading
- `src/main/java/com/mouad/dineops/dineOps/reservation/repository/ReservationRepository.java`
  - time-window status query for reminders
- `src/main/java/com/mouad/dineops/dineOps/order/repository/CustomerOrderRepository.java`
  - completed orders by branch/time range
- `src/main/java/com/mouad/dineops/dineOps/auth/repository/RefreshTokenRepository.java`
  - cleanup query for revoked+expired tokens

---

## 6. Verification Status

Phase 8 was verified via compile + local smoke tests:

- `mvn -q -DskipTests compile` success
- notification APIs tested (`/internal`, `/test-email`)
- reservation approval trigger tested with notification path intact

Result: the platform now includes automated operational notifications and scheduled maintenance/reporting flows.
