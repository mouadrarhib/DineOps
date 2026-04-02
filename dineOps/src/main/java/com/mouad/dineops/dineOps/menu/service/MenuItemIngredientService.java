package com.mouad.dineops.dineOps.menu.service;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.ConflictException;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.inventory.entity.Ingredient;
import com.mouad.dineops.dineOps.inventory.repository.IngredientRepository;
import com.mouad.dineops.dineOps.menu.dto.LinkMenuItemIngredientRequest;
import com.mouad.dineops.dineOps.menu.dto.MenuItemIngredientResponse;
import com.mouad.dineops.dineOps.menu.entity.MenuItem;
import com.mouad.dineops.dineOps.menu.entity.MenuItemIngredient;
import com.mouad.dineops.dineOps.menu.repository.MenuItemIngredientRepository;
import com.mouad.dineops.dineOps.menu.repository.MenuItemRepository;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Service
public class MenuItemIngredientService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final MenuItemIngredientRepository menuItemIngredientRepository;
	private final MenuItemRepository menuItemRepository;
	private final IngredientRepository ingredientRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;

	public MenuItemIngredientService(
			MenuItemIngredientRepository menuItemIngredientRepository,
			MenuItemRepository menuItemRepository,
			IngredientRepository ingredientRepository,
			StaffAssignmentRepository staffAssignmentRepository) {
		this.menuItemIngredientRepository = menuItemIngredientRepository;
		this.menuItemRepository = menuItemRepository;
		this.ingredientRepository = ingredientRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
	}

	@Transactional
	public MenuItemIngredientResponse linkIngredientToMenuItem(LinkMenuItemIngredientRequest request) {
		if (request.quantityRequired() == null || request.quantityRequired().signum() <= 0) {
			throw new BadRequestException("Quantity required must be greater than zero");
		}

		MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
				.orElseThrow(() -> new NotFoundException("Menu item not found: " + request.menuItemId()));
		enforceBranchScope(menuItem.getCategory().getBranch().getId());

		Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
				.orElseThrow(() -> new NotFoundException("Ingredient not found: " + request.ingredientId()));

		if (menuItemIngredientRepository.existsByMenuItemIdAndIngredientId(menuItem.getId(), ingredient.getId())) {
			throw new ConflictException("Ingredient already linked to this menu item");
		}

		MenuItemIngredient mapping = new MenuItemIngredient();
		mapping.setMenuItem(menuItem);
		mapping.setIngredient(ingredient);
		mapping.setQuantityRequired(request.quantityRequired());

		return toResponse(menuItemIngredientRepository.save(mapping));
	}

	private MenuItemIngredientResponse toResponse(MenuItemIngredient mapping) {
		return new MenuItemIngredientResponse(
				mapping.getId(),
				mapping.getMenuItem().getId(),
				mapping.getIngredient().getId(),
				mapping.getIngredient().getName(),
				mapping.getIngredient().getUnit(),
				mapping.getQuantityRequired(),
				mapping.getCreatedAt(),
				mapping.getUpdatedAt());
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
