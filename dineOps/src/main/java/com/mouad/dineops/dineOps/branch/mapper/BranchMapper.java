package com.mouad.dineops.dineOps.branch.mapper;

import org.springframework.stereotype.Component;

import com.mouad.dineops.dineOps.branch.dto.BranchResponse;
import com.mouad.dineops.dineOps.branch.dto.CreateBranchRequest;
import com.mouad.dineops.dineOps.branch.dto.UpdateBranchRequest;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;

@Component
public class BranchMapper {

	public Branch toEntity(CreateBranchRequest request, Restaurant restaurant) {
		Branch branch = new Branch();
		branch.setRestaurant(restaurant);
		branch.setName(request.name());
		branch.setAddress(request.address());
		branch.setCity(request.city());
		branch.setPhone(request.phone());
		branch.setOpeningTime(request.openingTime());
		branch.setClosingTime(request.closingTime());
		branch.setStatus(request.status() == null ? BranchStatus.ACTIVE : request.status());
		return branch;
	}

	public void updateEntity(Branch branch, UpdateBranchRequest request, Restaurant restaurant) {
		branch.setRestaurant(restaurant);
		branch.setName(request.name());
		branch.setAddress(request.address());
		branch.setCity(request.city());
		branch.setPhone(request.phone());
		branch.setOpeningTime(request.openingTime());
		branch.setClosingTime(request.closingTime());
		branch.setStatus(request.status() == null ? BranchStatus.ACTIVE : request.status());
	}

	public BranchResponse toResponse(Branch branch) {
		return new BranchResponse(
				branch.getId(),
				branch.getRestaurant().getId(),
				branch.getName(),
				branch.getAddress(),
				branch.getCity(),
				branch.getPhone(),
				branch.getOpeningTime(),
				branch.getClosingTime(),
				branch.getStatus(),
				branch.getCreatedAt(),
				branch.getUpdatedAt());
	}
}
