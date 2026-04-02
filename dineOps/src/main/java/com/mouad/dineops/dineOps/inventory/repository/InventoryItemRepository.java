package com.mouad.dineops.dineOps.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mouad.dineops.dineOps.inventory.entity.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

	boolean existsByBranchIdAndIngredientId(Long branchId, Long ingredientId);

	Optional<InventoryItem> findByBranchIdAndIngredientId(Long branchId, Long ingredientId);

	List<InventoryItem> findByBranchIdOrderByIngredientNameAsc(Long branchId);

	@Query("""
			select ii
			from InventoryItem ii
			where ii.branch.id = :branchId
			  and ii.ingredient.minThreshold is not null
			  and ii.quantityAvailable <= ii.ingredient.minThreshold
			order by ii.quantityAvailable asc, ii.ingredient.name asc
			""")
	List<InventoryItem> findLowStockByBranchId(@Param("branchId") Long branchId);
}
