package com.mouad.dineops.dineOps.menu.service;

import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.audit.service.AuditLogService;
import com.mouad.dineops.dineOps.common.config.CacheConfig;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.common.response.PagedResponse;
import com.mouad.dineops.dineOps.menu.dto.CreateMenuCategoryRequest;
import com.mouad.dineops.dineOps.menu.dto.MenuCategoryResponse;
import com.mouad.dineops.dineOps.menu.dto.UpdateMenuCategoryRequest;
import com.mouad.dineops.dineOps.menu.entity.MenuCategory;
import com.mouad.dineops.dineOps.menu.repository.MenuCategoryRepository;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Service
public class MenuCategoryService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final MenuCategoryRepository menuCategoryRepository;
	private final BranchRepository branchRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;
	private final AuditLogService auditLogService;

	public MenuCategoryService(
			MenuCategoryRepository menuCategoryRepository,
			BranchRepository branchRepository,
			StaffAssignmentRepository staffAssignmentRepository,
			AuditLogService auditLogService) {
		this.menuCategoryRepository = menuCategoryRepository;
		this.branchRepository = branchRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
		this.auditLogService = auditLogService;
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CacheConfig.MENU_CATEGORIES_BY_BRANCH, allEntries = true),
			@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true) })
	public MenuCategoryResponse createCategory(CreateMenuCategoryRequest request) {
		enforceBranchScope(request.branchId());
		Branch branch = findBranch(request.branchId());

		MenuCategory category = new MenuCategory();
		category.setBranch(branch);
		category.setName(request.name());
		category.setDescription(request.description());
		category.setDisplayOrder(request.displayOrder());
		category.setActive(request.active() == null ? true : request.active());

		MenuCategory saved = menuCategoryRepository.save(category);
		auditLogService.log(
				"MENU_UPDATED",
				"MENU_CATEGORY",
				saved.getId(),
				saved.getBranch().getId(),
				"Updated menu category: " + saved.getName());
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	@Cacheable(
			value = CacheConfig.MENU_CATEGORIES_BY_BRANCH,
			key = "#branchId + ':' + #active + ':' + #search + ':' + #page + ':' + #size")
	public PagedResponse<MenuCategoryResponse> listByBranch(
			Long branchId,
			Boolean active,
			String search,
			int page,
			int size) {
		enforceBranchScope(branchId);
		findBranch(branchId);

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("displayOrder").ascending().and(Sort.by("id").ascending()));
		Page<MenuCategory> rawPage = menuCategoryRepository.findPageByBranchAndFilters(branchId, active, search, pageRequest);
		Page<MenuCategoryResponse> responsePage = rawPage.map(this::toResponse);
		return PagedResponse.from(responsePage);
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CacheConfig.MENU_CATEGORIES_BY_BRANCH, allEntries = true),
			@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true) })
	public MenuCategoryResponse updateCategory(Long categoryId, UpdateMenuCategoryRequest request) {
		MenuCategory category = findCategory(categoryId);
		enforceBranchScope(category.getBranch().getId());

		category.setName(request.name());
		category.setDescription(request.description());
		category.setDisplayOrder(request.displayOrder());
		if (request.active() != null) {
			category.setActive(request.active());
		}

		MenuCategory saved = menuCategoryRepository.save(category);
		return toResponse(saved);
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CacheConfig.MENU_CATEGORIES_BY_BRANCH, allEntries = true),
			@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true) })
	public MenuCategoryResponse activateCategory(Long categoryId) {
		MenuCategory category = findCategory(categoryId);
		enforceBranchScope(category.getBranch().getId());
		category.setActive(true);
		return toResponse(menuCategoryRepository.save(category));
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CacheConfig.MENU_CATEGORIES_BY_BRANCH, allEntries = true),
			@CacheEvict(value = CacheConfig.MENU_ITEMS_BY_BRANCH, allEntries = true) })
	public MenuCategoryResponse deactivateCategory(Long categoryId) {
		MenuCategory category = findCategory(categoryId);
		enforceBranchScope(category.getBranch().getId());
		category.setActive(false);
		return toResponse(menuCategoryRepository.save(category));
	}

	private MenuCategory findCategory(Long categoryId) {
		return menuCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException("Menu category not found: " + categoryId));
	}

	private Branch findBranch(Long branchId) {
		return branchRepository.findById(branchId)
				.orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
	}

	private MenuCategoryResponse toResponse(MenuCategory category) {
		return new MenuCategoryResponse(
				category.getId(),
				category.getBranch().getId(),
				category.getName(),
				category.getDescription(),
				category.getDisplayOrder(),
				category.isActive(),
				category.getCreatedAt(),
				category.getUpdatedAt());
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
