package com.mouad.dineops.dineOps.staff.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mouad.dineops.dineOps.staff.entity.StaffAssignment;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {

	List<StaffAssignment> findByUserIdAndActiveTrue(Long userId);

	boolean existsByUserIdAndBranchIdAndActiveTrue(Long userId, Long branchId);

	boolean existsByUserIdAndBranchIdAndRoleNameAndActiveTrue(Long userId, Long branchId, String roleName);

	@Query("""
			select sa
			from StaffAssignment sa
			join fetch sa.user u
			where sa.branch.id = :branchId
			  and sa.role.name = :roleName
			  and sa.active = true
			""")
	List<StaffAssignment> findActiveByBranchIdAndRoleNameWithUser(
			@Param("branchId") Long branchId,
			@Param("roleName") String roleName);
}
