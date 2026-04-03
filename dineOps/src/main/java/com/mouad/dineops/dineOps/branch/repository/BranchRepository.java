package com.mouad.dineops.dineOps.branch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;

public interface BranchRepository extends JpaRepository<Branch, Long> {

	List<Branch> findByRestaurantId(Long restaurantId);

	List<Branch> findByStatus(BranchStatus status);
}
