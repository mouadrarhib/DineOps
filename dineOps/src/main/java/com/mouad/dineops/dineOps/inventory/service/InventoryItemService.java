package com.mouad.dineops.dineOps.inventory.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.ConflictException;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.inventory.dto.CreateInventoryItemRequest;
import com.mouad.dineops.dineOps.inventory.dto.InventoryItemResponse;
import com.mouad.dineops.dineOps.inventory.dto.RestockInventoryRequest;
import com.mouad.dineops.dineOps.inventory.entity.Ingredient;
import com.mouad.dineops.dineOps.inventory.entity.InventoryItem;
import com.mouad.dineops.dineOps.inventory.repository.IngredientRepository;
import com.mouad.dineops.dineOps.inventory.repository.InventoryItemRepository;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Service
public class InventoryItemService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final InventoryItemRepository inventoryItemRepository;
	private final BranchRepository branchRepository;
	private final IngredientRepository ingredientRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;

	public InventoryItemService(
			InventoryItemRepository inventoryItemRepository,
			BranchRepository branchRepository,
			IngredientRepository ingredientRepository,
			StaffAssignmentRepository staffAssignmentRepository) {
		this.inventoryItemRepository = inventoryItemRepository;
		this.branchRepository = branchRepository;
		this.ingredientRepository = ingredientRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
	}

	@Transactional
	public InventoryItemResponse createInventoryItem(CreateInventoryItemRequest request) {
		enforceBranchScope(request.branchId());
		validateNonNegative(request.quantityAvailable(), "Quantity available must be zero or positive");

		if (inventoryItemRepository.existsByBranchIdAndIngredientId(request.branchId(), request.ingredientId())) {
			throw new ConflictException("Inventory item already exists for this branch and ingredient");
		}

		Branch branch = findBranch(request.branchId());
		Ingredient ingredient = findIngredient(request.ingredientId());

		InventoryItem item = new InventoryItem();
		item.setBranch(branch);
		item.setIngredient(ingredient);
		item.setQuantityAvailable(request.quantityAvailable());
		if (request.quantityAvailable().signum() > 0) {
			item.setLastRestockedAt(Instant.now());
		}

		return toResponse(inventoryItemRepository.save(item));
	}

	@Transactional(readOnly = true)
	public List<InventoryItemResponse> listByBranch(Long branchId) {
		enforceBranchScope(branchId);
		findBranch(branchId);

		return inventoryItemRepository.findByBranchIdOrderByIngredientNameAsc(branchId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public InventoryItemResponse restock(Long inventoryItemId, RestockInventoryRequest request) {
		if (request.quantityAdded() == null || request.quantityAdded().signum() <= 0) {
			throw new BadRequestException("Restock quantity must be greater than zero");
		}

		InventoryItem item = findInventoryItem(inventoryItemId);
		enforceBranchScope(item.getBranch().getId());

		item.setQuantityAvailable(item.getQuantityAvailable().add(request.quantityAdded()));
		item.setLastRestockedAt(Instant.now());

		return toResponse(inventoryItemRepository.save(item));
	}

	@Transactional(readOnly = true)
	public List<InventoryItemResponse> listLowStockByBranch(Long branchId) {
		enforceBranchScope(branchId);
		findBranch(branchId);

		return inventoryItemRepository.findLowStockByBranchId(branchId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private InventoryItem findInventoryItem(Long inventoryItemId) {
		return inventoryItemRepository.findById(inventoryItemId)
				.orElseThrow(() -> new NotFoundException("Inventory item not found: " + inventoryItemId));
	}

	private Branch findBranch(Long branchId) {
		return branchRepository.findById(branchId)
				.orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
	}

	private Ingredient findIngredient(Long ingredientId) {
		return ingredientRepository.findById(ingredientId)
				.orElseThrow(() -> new NotFoundException("Ingredient not found: " + ingredientId));
	}

	private void validateNonNegative(BigDecimal value, String message) {
		if (value == null || value.signum() < 0) {
			throw new BadRequestException(message);
		}
	}

	private InventoryItemResponse toResponse(InventoryItem item) {
		return new InventoryItemResponse(
				item.getId(),
				item.getBranch().getId(),
				item.getIngredient().getId(),
				item.getIngredient().getName(),
				item.getIngredient().getUnit(),
				item.getQuantityAvailable(),
				item.getIngredient().getMinThreshold(),
				item.getLastRestockedAt(),
				item.getCreatedAt(),
				item.getUpdatedAt());
	}

	private void enforceBranchScope(Long branchId) {
		AppUserPrincipal principal = getCurrentUserPrincipal();
		if (principal == null) {
			return;
		}

		boolean branchScoped = principal.getRoles().stream().anyMatch(BRANCH_SCOPED_ROLES::contains);
		if (!branchScoped) {
			return;
		}

		boolean assigned = staffAssignmentRepository.existsByUserIdAndBranchIdAndActiveTrue(principal.getId(), branchId);
		if (!assigned) {
			throw new ForbiddenException("Access denied for this branch");
		}
	}

	private AppUserPrincipal getCurrentUserPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
			return null;
		}
		return principal;
	}
}
