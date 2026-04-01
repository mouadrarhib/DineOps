package com.mouad.dineops.dineOps.staff.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.entity.Role;
import com.mouad.dineops.dineOps.auth.repository.RoleRepository;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.staff.dto.AssignStaffRequest;
import com.mouad.dineops.dineOps.staff.dto.StaffAssignmentResponse;
import com.mouad.dineops.dineOps.staff.entity.StaffAssignment;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;
import com.mouad.dineops.dineOps.user.entity.User;
import com.mouad.dineops.dineOps.user.repository.UserRepository;

@Service
public class StaffAssignmentService {

	private static final Set<String> ALLOWED_ASSIGNABLE_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final StaffAssignmentRepository staffAssignmentRepository;
	private final UserRepository userRepository;
	private final BranchRepository branchRepository;
	private final RoleRepository roleRepository;

	public StaffAssignmentService(
			StaffAssignmentRepository staffAssignmentRepository,
			UserRepository userRepository,
			BranchRepository branchRepository,
			RoleRepository roleRepository) {
		this.staffAssignmentRepository = staffAssignmentRepository;
		this.userRepository = userRepository;
		this.branchRepository = branchRepository;
		this.roleRepository = roleRepository;
	}

	@Transactional
	public StaffAssignmentResponse assignStaffToBranch(AssignStaffRequest request) {
		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));

		Branch branch = branchRepository.findById(request.branchId())
				.orElseThrow(() -> new NotFoundException("Branch not found: " + request.branchId()));

		Role role = roleRepository.findById(request.roleId())
				.orElseThrow(() -> new NotFoundException("Role not found: " + request.roleId()));

		if (!ALLOWED_ASSIGNABLE_ROLES.contains(role.getName())) {
			throw new BadRequestException("Only staff operational roles can be assigned to a branch");
		}

		if (staffAssignmentRepository.existsByUserIdAndBranchIdAndRoleNameAndActiveTrue(
				user.getId(),
				branch.getId(),
				role.getName())) {
			throw new BadRequestException("Staff assignment already exists for this user, branch, and role");
		}

		StaffAssignment assignment = new StaffAssignment();
		assignment.setUser(user);
		assignment.setBranch(branch);
		assignment.setRole(role);
		assignment.setActive(request.active() == null ? true : request.active());

		StaffAssignment saved = staffAssignmentRepository.save(assignment);

		return new StaffAssignmentResponse(
				saved.getId(),
				saved.getUser().getId(),
				saved.getUser().getEmail(),
				saved.getBranch().getId(),
				saved.getBranch().getName(),
				saved.getRole().getId(),
				saved.getRole().getName(),
				saved.isActive(),
				saved.getCreatedAt());
	}
}
