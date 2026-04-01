package com.mouad.dineops.dineOps.branch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.branch.entity.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long> {

	List<Branch> findByRestaurantId(Long restaurantId);
}
