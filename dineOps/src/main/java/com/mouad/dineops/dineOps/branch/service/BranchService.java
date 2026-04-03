package com.mouad.dineops.dineOps.branch.service;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.branch.dto.BranchResponse;
import com.mouad.dineops.dineOps.branch.dto.CreateBranchRequest;
import com.mouad.dineops.dineOps.branch.dto.UpdateBranchRequest;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.mapper.BranchMapper;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.config.CacheConfig;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;
import com.mouad.dineops.dineOps.restaurant.repository.RestaurantRepository;
import com.mouad.dineops.dineOps.staff.entity.StaffAssignment;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Service
public class BranchService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final BranchRepository branchRepository;
	private final RestaurantRepository restaurantRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;
	private final BranchMapper branchMapper;

	public BranchService(
			BranchRepository branchRepository,
			RestaurantRepository restaurantRepository,
			StaffAssignmentRepository staffAssignmentRepository,
			BranchMapper branchMapper) {
		this.branchRepository = branchRepository;
		this.restaurantRepository = restaurantRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
		this.branchMapper = branchMapper;
	}

	@Transactional
	@CacheEvict(value = CacheConfig.BRANCH_DETAILS, allEntries = true)
	public BranchResponse createBranch(CreateBranchRequest request) {
		Restaurant restaurant = findRestaurant(request.restaurantId());
		Branch branch = branchMapper.toEntity(request, restaurant);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<BranchResponse> listBranches(Long restaurantId) {
		AppUserPrincipal principal = getCurrentUserPrincipal();

		if (isBranchScopedUser(principal)) {
			List<Branch> assignedBranches = staffAssignmentRepository.findByUserIdAndActiveTrue(principal.getId())
					.stream()
					.map(StaffAssignment::getBranch)
					.filter(branch -> restaurantId == null || branch.getRestaurant().getId().equals(restaurantId))
					.toList();

			return assignedBranches.stream().map(branchMapper::toResponse).toList();
		}

		List<Branch> branches = restaurantId == null
				? branchRepository.findAll()
				: branchRepository.findByRestaurantId(restaurantId);

		return branches.stream().map(branchMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	@Cacheable(value = CacheConfig.BRANCH_DETAILS, key = "#branchId")
	public BranchResponse getBranchById(Long branchId) {
		Branch branch = findBranch(branchId);
		enforceBranchScope(branch.getId());
		return branchMapper.toResponse(branch);
	}

	@Transactional
	@CacheEvict(value = CacheConfig.BRANCH_DETAILS, allEntries = true)
	public BranchResponse updateBranch(Long branchId, UpdateBranchRequest request) {
		Branch branch = findBranch(branchId);
		Restaurant restaurant = findRestaurant(request.restaurantId());
		branchMapper.updateEntity(branch, request, restaurant);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	@Transactional
	@CacheEvict(value = CacheConfig.BRANCH_DETAILS, allEntries = true)
	public BranchResponse activateBranch(Long branchId) {
		Branch branch = findBranch(branchId);
		branch.setStatus(BranchStatus.ACTIVE);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	@Transactional
	@CacheEvict(value = CacheConfig.BRANCH_DETAILS, allEntries = true)
	public BranchResponse deactivateBranch(Long branchId) {
		Branch branch = findBranch(branchId);
		branch.setStatus(BranchStatus.INACTIVE);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	private Branch findBranch(Long branchId) {
		return branchRepository.findById(branchId)
				.orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
	}

	private Restaurant findRestaurant(Long restaurantId) {
		return restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));
	}

	private AppUserPrincipal getCurrentUserPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
			return null;
		}
		return principal;
	}

	private boolean isBranchScopedUser(AppUserPrincipal principal) {
		if (principal == null) {
			return false;
		}
		return principal.getRoles().stream().anyMatch(BRANCH_SCOPED_ROLES::contains);
	}

	private void enforceBranchScope(Long branchId) {
		AppUserPrincipal principal = getCurrentUserPrincipal();
		if (!isBranchScopedUser(principal)) {
			return;
		}

		boolean allowed = staffAssignmentRepository.existsByUserIdAndBranchIdAndActiveTrue(principal.getId(), branchId);
		if (!allowed) {
			throw new ForbiddenException("Access denied for this branch");
		}
	}
}
