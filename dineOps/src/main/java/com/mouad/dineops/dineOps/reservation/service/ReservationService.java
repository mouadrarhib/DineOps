package com.mouad.dineops.dineOps.reservation.service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.common.enums.ReservationStatus;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.notification.service.NotificationService;
import com.mouad.dineops.dineOps.reservation.dto.CreateReservationRequest;
import com.mouad.dineops.dineOps.reservation.dto.ReservationResponse;
import com.mouad.dineops.dineOps.reservation.entity.Reservation;
import com.mouad.dineops.dineOps.reservation.repository.ReservationRepository;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Service
public class ReservationService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private final ReservationRepository reservationRepository;
	private final BranchRepository branchRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;
	private final NotificationService notificationService;

	public ReservationService(
			ReservationRepository reservationRepository,
			BranchRepository branchRepository,
			StaffAssignmentRepository staffAssignmentRepository,
			NotificationService notificationService) {
		this.reservationRepository = reservationRepository;
		this.branchRepository = branchRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
		this.notificationService = notificationService;
	}

	@Transactional
	public ReservationResponse createReservation(CreateReservationRequest request) {
		enforceBranchScope(request.branchId());
		Branch branch = findBranch(request.branchId());
		ensureBranchActive(branch);
		validateReservationTime(branch, request.reservationTime());
		validateGuestCount(request.numberOfGuests());

		Reservation reservation = new Reservation();
		reservation.setBranch(branch);
		reservation.setCustomerName(request.customerName().trim());
		reservation.setCustomerPhone(request.customerPhone().trim());
		reservation.setCustomerEmail(normalizeEmail(request.customerEmail()));
		reservation.setReservationTime(request.reservationTime());
		reservation.setNumberOfGuests(request.numberOfGuests());
		reservation.setStatus(ReservationStatus.PENDING);
		reservation.setNotes(request.notes());

		return toResponse(reservationRepository.save(reservation));
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> listReservations(Long branchId, ReservationStatus status) {
		enforceBranchScope(branchId);
		findBranch(branchId);

		List<Reservation> reservations = status == null
				? reservationRepository.findByBranchIdOrderByReservationTimeAsc(branchId)
				: reservationRepository.findByBranchIdAndStatusOrderByReservationTimeAsc(branchId, status);

		return reservations.stream().map(this::toResponse).toList();
	}

	@Transactional
	public ReservationResponse approveReservation(Long reservationId) {
		Reservation reservation = findReservation(reservationId);
		enforceBranchScope(reservation.getBranch().getId());
		validateTransition(reservation.getStatus(), ReservationStatus.APPROVED);
		ensureBranchActive(reservation.getBranch());
		validateReservationTime(reservation.getBranch(), reservation.getReservationTime());

		reservation.setStatus(ReservationStatus.APPROVED);
		Reservation saved = reservationRepository.save(reservation);

		notificationService.sendInternal(
				"reservation:" + saved.getId(),
				"Reservation approved",
				"Reservation for " + saved.getCustomerName() + " has been approved.",
				"RESERVATION",
				saved.getId());

		if (saved.getCustomerEmail() != null) {
			notificationService.sendEmail(
					saved.getCustomerEmail(),
					"Your reservation is approved",
					"Hello " + saved.getCustomerName()
							+ ", your reservation at " + saved.getReservationTime() + " has been approved.",
					"RESERVATION",
					saved.getId());
		}

		return toResponse(saved);
	}

	@Transactional
	public ReservationResponse rejectReservation(Long reservationId) {
		Reservation reservation = findReservation(reservationId);
		enforceBranchScope(reservation.getBranch().getId());
		validateTransition(reservation.getStatus(), ReservationStatus.REJECTED);

		reservation.setStatus(ReservationStatus.REJECTED);
		return toResponse(reservationRepository.save(reservation));
	}

	private void validateTransition(ReservationStatus from, ReservationStatus to) {
		if (from != ReservationStatus.PENDING) {
			throw new BadRequestException("Invalid reservation status transition from " + from + " to " + to);
		}
	}

	private void validateReservationTime(Branch branch, Instant reservationTime) {
		if (reservationTime == null) {
			throw new BadRequestException("Reservation time is required");
		}
		if (!reservationTime.isAfter(Instant.now())) {
			throw new BadRequestException("Reservation time must be in the future");
		}

		if (branch.getOpeningTime() != null && branch.getClosingTime() != null) {
			LocalTime reservationLocalTime = reservationTime.atZone(ZoneOffset.UTC).toLocalTime();
			if (!isWithinOperatingHours(reservationLocalTime, branch.getOpeningTime(), branch.getClosingTime())) {
				throw new BadRequestException("Reservation time is outside branch operating hours");
			}
		}
	}

	private boolean isWithinOperatingHours(LocalTime candidate, LocalTime opening, LocalTime closing) {
		if (opening.equals(closing)) {
			return true;
		}
		if (opening.isBefore(closing)) {
			return !candidate.isBefore(opening) && !candidate.isAfter(closing);
		}
		return !candidate.isBefore(opening) || !candidate.isAfter(closing);
	}

	private void validateGuestCount(Integer numberOfGuests) {
		if (numberOfGuests == null || numberOfGuests <= 0) {
			throw new BadRequestException("Number of guests must be greater than zero");
		}
	}

	private String normalizeEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return null;
		}
		return email.trim().toLowerCase();
	}

	private Branch findBranch(Long branchId) {
		return branchRepository.findById(branchId)
				.orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
	}

	private Reservation findReservation(Long reservationId) {
		return reservationRepository.findById(reservationId)
				.orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
	}

	private void ensureBranchActive(Branch branch) {
		if (branch.getStatus() != BranchStatus.ACTIVE) {
			throw new BadRequestException("Reservations are only allowed for active branches");
		}
	}

	private ReservationResponse toResponse(Reservation reservation) {
		return new ReservationResponse(
				reservation.getId(),
				reservation.getBranch().getId(),
				reservation.getCustomerName(),
				reservation.getCustomerPhone(),
				reservation.getCustomerEmail(),
				reservation.getReservationTime(),
				reservation.getNumberOfGuests(),
				reservation.getStatus(),
				reservation.getNotes(),
				reservation.getCreatedAt(),
				reservation.getUpdatedAt());
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
