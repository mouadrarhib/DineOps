# DineOps Phase 5 — Inventory and Ingredients

## Purpose

This document describes the Phase 5 implementation for inventory and ingredient management in DineOps, including domain modeling, APIs, mapping between menu items and ingredients, and core inventory business rules.

---

## 1. Scope Covered

Phase 5 includes four parts:

- Ingredient Domain
- Inventory Domain
- Menu Item Ingredient Mapping
- Business Rules

---

## 2. Ingredient Domain

## Entity

### `Ingredient`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/entity/Ingredient.java`

Fields:
- `id`
- `name` (unique, required, max 150)
- `unit` (required, max 30)
- `minThreshold` (`NUMERIC(12,3)`, optional)
- `active` (required)
- `createdAt`, `updatedAt`

## Repository

### `IngredientRepository`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/repository/IngredientRepository.java`

Key methods:
- `existsByNameIgnoreCase(String name)`
- `findByActiveTrueOrderByNameAsc()`

## Service

### `IngredientService`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/service/IngredientService.java`

Responsibilities:
- normalize ingredient name (`trim`)
- normalize unit (`trim` + uppercase)
- reject duplicates with `409 Conflict`
- list all or active-only ingredients

## Controller and Endpoints

### `IngredientController`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/controller/IngredientController.java`

Base path: `/api/ingredients`

- `POST /api/ingredients`
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`
  - creates ingredient
- `GET /api/ingredients?activeOnly={boolean}`
  - lists ingredients

DTOs:
- `CreateIngredientRequest`
- `IngredientResponse`

---

## 3. Inventory Domain

## Entity

### `InventoryItem`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/entity/InventoryItem.java`

Relationships:
- many-to-one `Branch`
- many-to-one `Ingredient`

Fields:
- `quantityAvailable` (`NUMERIC(12,3)`, required)
- `lastRestockedAt` (optional)

Constraint:
- unique `(branch_id, ingredient_id)`

## Repository

### `InventoryItemRepository`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/repository/InventoryItemRepository.java`

Key methods:
- `existsByBranchIdAndIngredientId(...)`
- `findByBranchIdAndIngredientId(...)`
- `findByBranchIdOrderByIngredientNameAsc(...)`
- `findLowStockByBranchId(...)`

## Service

### `InventoryItemService`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/service/InventoryItemService.java`

Responsibilities:
- create inventory item with branch+ingredient linkage
- list inventory by branch
- restock inventory item
- list low-stock items by branch
- enforce branch-scoped access for branch-scoped roles

## Controller and Endpoints

### `InventoryItemController`
Path: `src/main/java/com/mouad/dineops/dineOps/inventory/controller/InventoryItemController.java`

Base path: `/api/inventory-items`

- `POST /api/inventory-items`
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`
  - create inventory item
- `GET /api/inventory-items?branchId={id}`
  - list branch inventory
- `PATCH /api/inventory-items/{inventoryItemId}/restock`
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`
  - increase available quantity
- `GET /api/inventory-items/low-stock?branchId={id}`
  - list low-stock inventory for branch

DTOs:
- `CreateInventoryItemRequest`
- `RestockInventoryRequest`
- `InventoryItemResponse`

---

## 4. Menu Item Ingredient Mapping

## Entity

### `MenuItemIngredient`
Path: `src/main/java/com/mouad/dineops/dineOps/menu/entity/MenuItemIngredient.java`

Relationships:
- many-to-one `MenuItem`
- many-to-one `Ingredient`

Fields:
- `quantityRequired` (`NUMERIC(12,3)`, required)

Constraint:
- unique `(menu_item_id, ingredient_id)`

## Repository

### `MenuItemIngredientRepository`
Path: `src/main/java/com/mouad/dineops/dineOps/menu/repository/MenuItemIngredientRepository.java`

Key method:
- `existsByMenuItemIdAndIngredientId(...)`

## Service

### `MenuItemIngredientService`
Path: `src/main/java/com/mouad/dineops/dineOps/menu/service/MenuItemIngredientService.java`

Responsibilities:
- link ingredient to menu item
- validate `quantityRequired > 0`
- prevent duplicate mapping for same menu item + ingredient
- enforce branch-scope check from target menu item branch

## Controller and Endpoint

### `MenuItemIngredientController`
Path: `src/main/java/com/mouad/dineops/dineOps/menu/controller/MenuItemIngredientController.java`

Base path: `/api/menu-item-ingredients`

- `POST /api/menu-item-ingredients`
  - roles: `SUPER_ADMIN`, `RESTAURANT_OWNER`, `BRANCH_MANAGER`
  - link ingredient to menu item

DTOs:
- `LinkMenuItemIngredientRequest`
- `MenuItemIngredientResponse`

---

## 5. Business Rules

Implemented in `InventoryItemService`:

## Prevent Negative Stock
- stock cannot be created with negative quantity
- restock quantity must be positive
- resulting stock is validated to remain non-negative

## Inventory Consistency Validations
- inventory operations require an active branch
- inventory operations require an active ingredient
- ingredient `minThreshold` (if present) must be non-negative
- quantity values are constrained to at most 3 decimal places

## Low-Stock Threshold Logic
- low-stock condition is: `quantityAvailable <= threshold`
- if `minThreshold` is null, threshold defaults to `0`
- low-stock listing applies the same threshold rule consistently

---

## 6. Error Behavior

Common HTTP outcomes for this phase:

- `400 Bad Request`
  - invalid quantities/threshold values
  - inactive branch or ingredient for inventory operations
- `403 Forbidden`
  - branch-scoped user trying to access unassigned branch
- `404 Not Found`
  - missing branch, ingredient, menu item, or inventory item
- `409 Conflict`
  - duplicate inventory item for same branch+ingredient
  - duplicate menu item ingredient mapping

All responses use the standard `ApiResponse<T>` envelope.

---

## 7. Verification Status

Phase 5 APIs were compiled and smoke-tested locally, including:

- ingredient creation/listing
- inventory create/list/restock/low-stock
- menu item ingredient linking
- duplicate and validation error paths
