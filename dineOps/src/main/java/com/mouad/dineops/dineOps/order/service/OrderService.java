package com.mouad.dineops.dineOps.order.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.common.enums.OrderStatus;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.ForbiddenException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.menu.entity.MenuItem;
import com.mouad.dineops.dineOps.menu.repository.MenuItemRepository;
import com.mouad.dineops.dineOps.order.dto.CreateOrderItemRequest;
import com.mouad.dineops.dineOps.order.dto.CreateOrderRequest;
import com.mouad.dineops.dineOps.order.dto.OrderItemResponse;
import com.mouad.dineops.dineOps.order.dto.OrderResponse;
import com.mouad.dineops.dineOps.order.dto.OrderSummaryResponse;
import com.mouad.dineops.dineOps.order.entity.CustomerOrder;
import com.mouad.dineops.dineOps.order.entity.OrderItem;
import com.mouad.dineops.dineOps.order.repository.CustomerOrderRepository;
import com.mouad.dineops.dineOps.order.repository.OrderItemRepository;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;
import com.mouad.dineops.dineOps.user.entity.User;
import com.mouad.dineops.dineOps.user.repository.UserRepository;

@Service
public class OrderService {

	private static final Set<String> BRANCH_SCOPED_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER.name(),
			SystemRole.CASHIER.name(),
			SystemRole.KITCHEN_STAFF.name());

	private static final Set<String> ALLOWED_SOURCES = Set.of("IN_STORE", "PHONE", "ONLINE");

	private static final DateTimeFormatter ORDER_NUMBER_TIME_FORMAT =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

	private final CustomerOrderRepository customerOrderRepository;
	private final OrderItemRepository orderItemRepository;
	private final BranchRepository branchRepository;
	private final MenuItemRepository menuItemRepository;
	private final UserRepository userRepository;
	private final StaffAssignmentRepository staffAssignmentRepository;

	public OrderService(
			CustomerOrderRepository customerOrderRepository,
			OrderItemRepository orderItemRepository,
			BranchRepository branchRepository,
			MenuItemRepository menuItemRepository,
			UserRepository userRepository,
			StaffAssignmentRepository staffAssignmentRepository) {
		this.customerOrderRepository = customerOrderRepository;
		this.orderItemRepository = orderItemRepository;
		this.branchRepository = branchRepository;
		this.menuItemRepository = menuItemRepository;
		this.userRepository = userRepository;
		this.staffAssignmentRepository = staffAssignmentRepository;
	}

	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		Branch branch = findBranch(request.branchId());
		enforceBranchScope(branch.getId());
		ensureBranchActive(branch);

		if (request.items() == null || request.items().isEmpty()) {
			throw new BadRequestException("Order must contain at least one item");
		}

		String normalizedSource = normalizeSource(request.source());
		BigDecimal taxAmount = normalizeTaxAmount(request.taxAmount());

		User createdBy = getCurrentUser();

		BigDecimal subtotal = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		for (CreateOrderItemRequest itemRequest : request.items()) {
			if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
				throw new BadRequestException("Order item quantity must be greater than zero");
			}

			MenuItem menuItem = findMenuItem(itemRequest.menuItemId());
			validateOrderMenuItem(branch.getId(), menuItem);

			BigDecimal unitPrice = menuItem.getPrice();
			BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

			OrderItem orderItem = new OrderItem();
			orderItem.setMenuItem(menuItem);
			orderItem.setQuantity(itemRequest.quantity());
			orderItem.setUnitPrice(unitPrice);
			orderItem.setTotalPrice(lineTotal);
			orderItem.setNotes(itemRequest.notes());

			orderItems.add(orderItem);
			subtotal = subtotal.add(lineTotal);
		}

		CustomerOrder order = new CustomerOrder();
		order.setBranch(branch);
		order.setOrderNumber(generateOrderNumber());
		order.setStatus(OrderStatus.PENDING);
		order.setSource(normalizedSource);
		order.setSubtotal(subtotal);
		order.setTaxAmount(taxAmount);
		order.setTotalAmount(subtotal.add(taxAmount));
		order.setNotes(request.notes());
		order.setCreatedBy(createdBy);

		CustomerOrder savedOrder = customerOrderRepository.save(order);
		for (OrderItem orderItem : orderItems) {
			orderItem.setOrder(savedOrder);
		}
		List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);

		return toOrderResponse(savedOrder, savedItems);
	}

	@Transactional(readOnly = true)
	public List<OrderSummaryResponse> listOrders(Long branchId, OrderStatus status) {
		findBranch(branchId);
		enforceBranchScope(branchId);

		List<CustomerOrder> orders = status == null
				? customerOrderRepository.findByBranchIdOrderByCreatedAtDesc(branchId)
				: customerOrderRepository.findByBranchIdAndStatusOrderByCreatedAtDesc(branchId, status);

		return orders.stream()
				.map(this::toOrderSummaryResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrderDetails(Long orderId) {
		CustomerOrder order = findOrder(orderId);
		enforceBranchScope(order.getBranch().getId());
		List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
		return toOrderResponse(order, items);
	}

	@Transactional
	public OrderResponse confirmOrder(Long orderId) {
		return transitionOrder(orderId, OrderStatus.PENDING, OrderStatus.CONFIRMED);
	}

	@Transactional
	public OrderResponse startPreparation(Long orderId) {
		return transitionOrder(orderId, OrderStatus.CONFIRMED, OrderStatus.IN_PREPARATION);
	}

	@Transactional
	public OrderResponse readyOrder(Long orderId) {
		return transitionOrder(orderId, OrderStatus.IN_PREPARATION, OrderStatus.READY);
	}

	@Transactional
	public OrderResponse completeOrder(Long orderId) {
		return transitionOrder(orderId, OrderStatus.READY, OrderStatus.COMPLETED);
	}

	@Transactional
	public OrderResponse cancelOrder(Long orderId) {
		CustomerOrder order = findOrder(orderId);
		enforceBranchScope(order.getBranch().getId());

		if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED) {
			throw new BadRequestException("Order in status " + order.getStatus() + " cannot be canceled");
		}

		order.setStatus(OrderStatus.CANCELED);
		CustomerOrder saved = customerOrderRepository.save(order);
		List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(saved.getId());
		return toOrderResponse(saved, items);
	}

	private OrderResponse transitionOrder(Long orderId, OrderStatus from, OrderStatus to) {
		CustomerOrder order = findOrder(orderId);
		enforceBranchScope(order.getBranch().getId());

		if (order.getStatus() != from) {
			throw new BadRequestException("Invalid order status transition from " + order.getStatus() + " to " + to);
		}

		order.setStatus(to);
		if (to == OrderStatus.CONFIRMED) {
			order.setConfirmedAt(Instant.now());
		}
		if (to == OrderStatus.COMPLETED) {
			order.setCompletedAt(Instant.now());
		}

		CustomerOrder saved = customerOrderRepository.save(order);
		List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(saved.getId());
		return toOrderResponse(saved, items);
	}

	private Branch findBranch(Long branchId) {
		return branchRepository.findById(branchId)
				.orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
	}

	private CustomerOrder findOrder(Long orderId) {
		return customerOrderRepository.findById(orderId)
				.orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
	}

	private MenuItem findMenuItem(Long menuItemId) {
		return menuItemRepository.findById(menuItemId)
				.orElseThrow(() -> new NotFoundException("Menu item not found: " + menuItemId));
	}

	private User getCurrentUser() {
		AppUserPrincipal principal = getCurrentUserPrincipal();
		if (principal == null) {
			throw new ForbiddenException("Authentication is required");
		}
		return userRepository.findById(principal.getId())
				.orElseThrow(() -> new NotFoundException("User not found: " + principal.getId()));
	}

	private void validateOrderMenuItem(Long branchId, MenuItem menuItem) {
		if (!menuItem.getCategory().getBranch().getId().equals(branchId)) {
			throw new BadRequestException("Menu item does not belong to the specified branch");
		}
		if (!menuItem.isActive() || !menuItem.isAvailable()) {
			throw new BadRequestException("Menu item is not available for ordering: " + menuItem.getName());
		}
	}

	private String normalizeSource(String source) {
		if (source == null || source.trim().isEmpty()) {
			throw new BadRequestException("Order source is required");
		}
		String normalized = source.trim().toUpperCase(Locale.ROOT);
		if (!ALLOWED_SOURCES.contains(normalized)) {
			throw new BadRequestException("Unsupported order source: " + source);
		}
		return normalized;
	}

	private BigDecimal normalizeTaxAmount(BigDecimal taxAmount) {
		BigDecimal normalized = taxAmount == null ? BigDecimal.ZERO : taxAmount;
		if (normalized.signum() < 0) {
			throw new BadRequestException("Tax amount must be zero or positive");
		}
		if (normalized.scale() > 2) {
			throw new BadRequestException("Tax amount can have at most 2 decimal places");
		}
		return normalized;
	}

	private String generateOrderNumber() {
		for (int attempt = 0; attempt < 10; attempt++) {
			String candidate = "ORD-" + ORDER_NUMBER_TIME_FORMAT.format(Instant.now()) + "-"
					+ ThreadLocalRandom.current().nextInt(1000, 10000);
			if (!customerOrderRepository.existsByOrderNumber(candidate)) {
				return candidate;
			}
		}
		throw new BadRequestException("Unable to generate unique order number");
	}

	private void ensureBranchActive(Branch branch) {
		if (branch.getStatus() != BranchStatus.ACTIVE) {
			throw new BadRequestException("Orders can only be created for active branches");
		}
	}

	private OrderSummaryResponse toOrderSummaryResponse(CustomerOrder order) {
		return new OrderSummaryResponse(
				order.getId(),
				order.getBranch().getId(),
				order.getOrderNumber(),
				order.getStatus(),
				order.getSource(),
				order.getSubtotal(),
				order.getTaxAmount(),
				order.getTotalAmount(),
				order.getNotes(),
				order.getCreatedBy().getId(),
				order.getCreatedAt(),
				order.getConfirmedAt(),
				order.getCompletedAt());
	}

	private OrderResponse toOrderResponse(CustomerOrder order, List<OrderItem> items) {
		List<OrderItemResponse> itemResponses = items.stream()
				.map(item -> new OrderItemResponse(
						item.getId(),
						item.getMenuItem().getId(),
						item.getMenuItem().getName(),
						item.getQuantity(),
						item.getUnitPrice(),
						item.getTotalPrice(),
						item.getNotes()))
				.toList();

		return new OrderResponse(
				order.getId(),
				order.getBranch().getId(),
				order.getOrderNumber(),
				order.getStatus(),
				order.getSource(),
				order.getSubtotal(),
				order.getTaxAmount(),
				order.getTotalAmount(),
				order.getNotes(),
				order.getCreatedBy().getId(),
				order.getCreatedAt(),
				order.getConfirmedAt(),
				order.getCompletedAt(),
				itemResponses);
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
