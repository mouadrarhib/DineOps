package com.mouad.dineops.dineOps.inventory.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.inventory.dto.CreateInventoryItemRequest;
import com.mouad.dineops.dineOps.inventory.dto.InventoryItemResponse;
import com.mouad.dineops.dineOps.inventory.dto.RestockInventoryRequest;
import com.mouad.dineops.dineOps.inventory.service.InventoryItemService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/inventory-items")
public class InventoryItemController {

	private final InventoryItemService inventoryItemService;

	public InventoryItemController(InventoryItemService inventoryItemService) {
		this.inventoryItemService = inventoryItemService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<InventoryItemResponse> createInventoryItem(@Valid @RequestBody CreateInventoryItemRequest request) {
		return ApiResponse.success("Inventory item created successfully", inventoryItemService.createInventoryItem(request));
	}

	@GetMapping
	public ApiResponse<List<InventoryItemResponse>> listByBranch(@RequestParam Long branchId) {
		return ApiResponse.success("Inventory items fetched successfully", inventoryItemService.listByBranch(branchId));
	}

	@PatchMapping("/{inventoryItemId}/restock")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<InventoryItemResponse> restock(
			@PathVariable Long inventoryItemId,
			@Valid @RequestBody RestockInventoryRequest request) {
		return ApiResponse.success("Inventory item restocked successfully", inventoryItemService.restock(inventoryItemId, request));
	}

	@GetMapping("/low-stock")
	public ApiResponse<List<InventoryItemResponse>> listLowStockByBranch(@RequestParam Long branchId) {
		return ApiResponse.success("Low-stock inventory fetched successfully", inventoryItemService.listLowStockByBranch(branchId));
	}
}
