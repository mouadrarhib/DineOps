package com.mouad.dineops.dineOps.menu.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.audit.service.AuditLogService;
import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.common.config.CacheConfig;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.common.response.PagedResponse;
import com.mouad.dineops.dineOps.menu.dto.CreateMenuItemRequest;
import com.mouad.dineops.dineOps.menu.dto.MenuItemResponse;
import com.mouad.dineops.dineOps.menu.dto.UpdateMenuItemRequest;
import com.mouad.dineops.dineOps.menu.entity.MenuCategory;
import com.mouad.dineops.dineOps.menu.entity.MenuItem;
import com.mouad.dineops.dineOps.menu.repository.MenuCategoryRepository;
import com.mouad.dineops.dineOps.menu.repository.MenuItemRepository;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Service
public class MenuItemService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final MenuItemRepository menuItemRepository;
	private final MenuCategoryRepository menuCategoryRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;
	private final AuditLogService auditLogService;

	public MenuItemService(
			MenuItemRepository menuItemRepository,
			MenuCategoryRepository menuCategoryRepository,
			StaffAssignmentRepository staffAssignmentRepository,
			AuditLogService auditLogService) {
		this.menuItemRepository = menuItemRepository;
		this.menuCategoryRepository = menuCategoryRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
		this.auditLogService = auditLogService;
	}

	@Transactional
	@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true)
	public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
		enforceBranchScope(request.branchId());
		MenuCategory category = findCategory(request.categoryId());
		validateCategoryBranch(category, request.branchId());
		ensureCategoryIsActive(category);
		validatePrice(request.price());

		MenuItem item = new MenuItem();
		item.setCategory(category);
		item.setName(request.name());
		item.setDescription(request.description());
		item.setPrice(request.price());
		item.setAvailable(request.available() == null ? true : request.available());
		item.setPreparationTimeMinutes(request.preparationTimeMinutes());
		item.setActive(request.active() == null ? true : request.active());

		MenuItem saved = menuItemRepository.save(item);
		auditLogService.log(
				"MENU_UPDATED",
				"MENU_ITEM",
				saved.getId(),
				saved.getCategory().getBranch().getId(),
				"Updated menu item: " + saved.getName());
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	@Cacheable(
			value = CacheConfig.MENU_ITEMS_BY_BRANCH,
			key = "#branchId + ':' + #active + ':' + #available + ':' + #search + ':' + #page + ':' + #size")
	public PagedResponse<MenuItemResponse> listByBranch(
			Long branchId,
			Boolean active,
			Boolean available,
			String search,
			int page,
			int size) {
		enforceBranchScope(branchId);

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending().and(Sort.by("id").ascending()));
		Page<MenuItem> rawPage = menuItemRepository.findPageByBranchAndFilters(branchId, active, available, search, pageRequest);
		Page<MenuItemResponse> responsePage = rawPage.map(this::toResponse);
		return PagedResponse.from(responsePage);
	}

	@Transactional(readOnly = true)
	public MenuItemResponse getDetails(Long menuItemId) {
		MenuItem item = findMenuItem(menuItemId);
		enforceBranchScope(item.getCategory().getBranch().getId());
		return toResponse(item);
	}

	@Transactional
	@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true)
	public MenuItemResponse updateMenuItem(Long menuItemId, UpdateMenuItemRequest request) {
		MenuItem item = findMenuItem(menuItemId);
		MenuCategory category = findCategory(request.categoryId());
		enforceBranchScope(request.branchId());
		validateCategoryBranch(category, request.branchId());
		if (!item.getCategory().getBranch().getId().equals(request.branchId())) {
			throw new BadRequestException("Menu item does not belong to the provided branch");
		}
		ensureCategoryIsActive(category);
		validatePrice(request.price());

		item.setCategory(category);
		item.setName(request.name());
		item.setDescription(request.description());
		item.setPrice(request.price());
		if (request.available() != null) {
			item.setAvailable(request.available());
		}
		item.setPreparationTimeMinutes(request.preparationTimeMinutes());
		if (request.active() != null) {
			item.setActive(request.active());
		}

		return toResponse(menuItemRepository.save(item));
	}

	@Transactional
	@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true)
	public MenuItemResponse toggleAvailability(Long menuItemId, boolean available) {
		MenuItem item = findMenuItem(menuItemId);
		enforceBranchScope(item.getCategory().getBranch().getId());
		item.setAvailable(available);
		return toResponse(menuItemRepository.save(item));
	}

	private MenuItem findMenuItem(Long menuItemId) {
		return menuItemRepository.findById(menuItemId)
				.orElseThrow(() -> new NotFoundException("Menu item not found: " + menuItemId));
	}

	private MenuCategory findCategory(Long categoryId) {
		return menuCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException("Menu category not found: " + categoryId));
	}

	private void validateCategoryBranch(MenuCategory category, Long branchId) {
		if (!category.getBranch().getId().equals(branchId)) {
			throw new BadRequestException("Menu category does not belong to the specified branch");
		}
	}

	private void ensureCategoryIsActive(MenuCategory category) {
		if (!category.isActive()) {
			throw new BadRequestException("Inactive category cannot receive new menu items");
		}
	}

	private void validatePrice(java.math.BigDecimal price) {
		if (price == null || price.signum() <= 0) {
			throw new BadRequestException("Price must be positive");
		}
	}

	private MenuItemResponse toResponse(MenuItem item) {
		return new MenuItemResponse(
				item.getId(),
				item.getCategory().getId(),
				item.getCategory().getBranch().getId(),
				item.getName(),
				item.getDescription(),
				item.getPrice(),
				item.isAvailable(),
				item.getPreparationTimeMinutes(),
				item.isActive(),
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
