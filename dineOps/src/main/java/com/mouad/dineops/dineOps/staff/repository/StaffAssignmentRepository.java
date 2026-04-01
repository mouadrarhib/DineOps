package com.mouad.dineops.dineOps.staff.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.staff.entity.StaffAssignment;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {

	List<StaffAssignment> findByUserIdAndActiveTrue(Long userId);

	boolean existsByUserIdAndBranchIdAndActiveTrue(Long userId, Long branchId);

	boolean existsByUserIdAndBranchIdAndRoleNameAndActiveTrue(Long userId, Long branchId, String roleName);
}
