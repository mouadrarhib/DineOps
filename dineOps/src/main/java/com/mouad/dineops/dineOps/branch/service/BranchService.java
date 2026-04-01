package com.mouad.dineops.dineOps.branch.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.branch.dto.BranchResponse;
import com.mouad.dineops.dineOps.branch.dto.CreateBranchRequest;
import com.mouad.dineops.dineOps.branch.dto.UpdateBranchRequest;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.mapper.BranchMapper;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;
import com.mouad.dineops.dineOps.restaurant.repository.RestaurantRepository;

@Service
public class BranchService {

	private final BranchRepository branchRepository;
	private final RestaurantRepository restaurantRepository;
	private final BranchMapper branchMapper;

	public BranchService(
			BranchRepository branchRepository,
			RestaurantRepository restaurantRepository,
			BranchMapper branchMapper) {
		this.branchRepository = branchRepository;
		this.restaurantRepository = restaurantRepository;
		this.branchMapper = branchMapper;
	}

	@Transactional
	public BranchResponse createBranch(CreateBranchRequest request) {
		Restaurant restaurant = findRestaurant(request.restaurantId());
		Branch branch = branchMapper.toEntity(request, restaurant);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<BranchResponse> listBranches(Long restaurantId) {
		List<Branch> branches = restaurantId == null
				? branchRepository.findAll()
				: branchRepository.findByRestaurantId(restaurantId);

		return branches.stream().map(branchMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public BranchResponse getBranchById(Long branchId) {
		return branchMapper.toResponse(findBranch(branchId));
	}

	@Transactional
	public BranchResponse updateBranch(Long branchId, UpdateBranchRequest request) {
		Branch branch = findBranch(branchId);
		Restaurant restaurant = findRestaurant(request.restaurantId());
		branchMapper.updateEntity(branch, request, restaurant);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	@Transactional
	public BranchResponse activateBranch(Long branchId) {
		Branch branch = findBranch(branchId);
		branch.setStatus(BranchStatus.ACTIVE);
		Branch saved = branchRepository.save(branch);
		return branchMapper.toResponse(saved);
	}

	@Transactional
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
}
