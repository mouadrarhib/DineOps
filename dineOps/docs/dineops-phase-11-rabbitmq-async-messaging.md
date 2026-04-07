# DineOps Phase 11 — Asynchronous Processing

## Objective

Introduce event-driven behavior as an advanced backend engineering layer beyond synchronous CRUD.

---

## 1. Scope Implemented

Implemented tasks:

- configured RabbitMQ exchange, queues, and bindings
- created event publisher abstraction
- published `OrderConfirmedEvent`
- published `ReservationApprovedEvent`
- published `LowStockDetectedEvent`
- created notification event consumers
- moved reservation approval and low-stock notification dispatch to async consumers
- documented event flow in README

Optional tasks intentionally deferred:

- outbox table
- outbox pattern
- retry strategy for failed consumers

---

## 2. RabbitMQ Configuration

Path:

- `src/main/java/com/mouad/dineops/dineOps/common/config/RabbitMqConfig.java`

Configured components:

- topic exchange: `dineops.events.exchange`
- queue: `dineops.events.order-confirmed.queue`
- queue: `dineops.events.reservation-approved.queue`
- queue: `dineops.events.low-stock-detected.queue`
- routing keys:
  - `dineops.event.order.confirmed`
  - `dineops.event.reservation.approved`
  - `dineops.event.low-stock.detected`
- JSON message conversion with `Jackson2JsonMessageConverter`

All names are overrideable from `application.yml`.

---

## 3. Event Publisher Abstraction

Paths:

- `src/main/java/com/mouad/dineops/dineOps/common/messaging/EventPublisher.java`
- `src/main/java/com/mouad/dineops/dineOps/common/messaging/RabbitMqEventPublisher.java`

Behavior:

- application code publishes domain events through `EventPublisher`
- RabbitMQ implementation sends payloads to configured events exchange
- publishing is gated by `app.messaging.rabbit.enabled`

---

## 4. Published Domain Events

Event payload records:

- `src/main/java/com/mouad/dineops/dineOps/notification/event/OrderConfirmedEvent.java`
- `src/main/java/com/mouad/dineops/dineOps/notification/event/ReservationApprovedEvent.java`
- `src/main/java/com/mouad/dineops/dineOps/notification/event/LowStockDetectedEvent.java`

Publish points:

- `OrderService.confirmOrder(...)` publishes `OrderConfirmedEvent`
- `ReservationService.approveReservation(...)` publishes `ReservationApprovedEvent`
- `OperationsScheduledJobs.lowStockJob()` publishes `LowStockDetectedEvent`

---

## 5. Notification Event Consumers

Paths:

- `src/main/java/com/mouad/dineops/dineOps/notification/messaging/OrderConfirmedEventConsumer.java`
- `src/main/java/com/mouad/dineops/dineOps/notification/messaging/ReservationApprovedEventConsumer.java`
- `src/main/java/com/mouad/dineops/dineOps/notification/messaging/LowStockDetectedEventConsumer.java`

Behavior:

- all consumers are activated only when `app.messaging.rabbit.enabled=true`
- `OrderConfirmedEventConsumer` sends internal order-confirmed notification
- `ReservationApprovedEventConsumer` sends internal + optional email customer notification
- `LowStockDetectedEventConsumer` resolves branch managers and sends low-stock alerts

Result:

- reservation approval and low-stock notification workflows are now asynchronous.

---

## 6. Configuration Keys

Added/used properties in `src/main/resources/application.yml`:

- `spring.rabbitmq.host`
- `spring.rabbitmq.port`
- `spring.rabbitmq.username`
- `spring.rabbitmq.password`
- `app.messaging.rabbit.enabled`
- `app.messaging.events.exchange`
- `app.messaging.events.order-confirmed.queue`
- `app.messaging.events.order-confirmed.routing-key`
- `app.messaging.events.reservation-approved.queue`
- `app.messaging.events.reservation-approved.routing-key`
- `app.messaging.events.low-stock-detected.queue`
- `app.messaging.events.low-stock-detected.routing-key`

`docker-compose.yml` enables async mode via:

- `APP_MESSAGING_RABBIT_ENABLED=true`

---

## 7. Event Flow Overview

1. domain service or scheduled job emits a domain event
2. `EventPublisher` routes event to RabbitMQ exchange
3. event-specific queue receives message by routing key
4. consumer processes event and calls `NotificationService`
5. notifications are persisted and delivered using existing notification infrastructure

---

## 8. Verification Target

- compile/build success
- confirming an order enqueues and consumes `OrderConfirmedEvent`
- approving a reservation enqueues and consumes `ReservationApprovedEvent`
- low-stock job enqueues and consumes `LowStockDetectedEvent`
- notification records are created by consumers after event handling

Phase 11 deliverable achieved: DineOps now includes an event-driven asynchronous processing layer on RabbitMQ.
