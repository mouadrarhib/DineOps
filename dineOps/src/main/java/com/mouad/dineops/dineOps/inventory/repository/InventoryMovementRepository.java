package com.mouad.dineops.dineOps.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.inventory.entity.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
}
