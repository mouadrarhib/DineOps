package com.mouad.dineops.dineOps.order.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.enums.OrderStatus;
import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.order.dto.CreateOrderRequest;
import com.mouad.dineops.dineOps.order.dto.OrderResponse;
import com.mouad.dineops.dineOps.order.dto.OrderSummaryResponse;
import com.mouad.dineops.dineOps.order.service.OrderService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','CASHIER')")
	public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		return ApiResponse.success("Order created successfully", orderService.createOrder(request));
	}

	@GetMapping
	public ApiResponse<List<OrderSummaryResponse>> listOrders(
			@RequestParam Long branchId,
			@RequestParam(required = false) OrderStatus status) {
		return ApiResponse.success("Orders fetched successfully", orderService.listOrders(branchId, status));
	}

	@GetMapping("/{orderId}")
	public ApiResponse<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
		return ApiResponse.success("Order fetched successfully", orderService.getOrderDetails(orderId));
	}

	@PatchMapping("/{orderId}/confirm")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','CASHIER')")
	public ApiResponse<OrderResponse> confirmOrder(@PathVariable Long orderId) {
		return ApiResponse.success("Order confirmed successfully", orderService.confirmOrder(orderId));
	}

	@PatchMapping("/{orderId}/start-preparation")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','KITCHEN_STAFF')")
	public ApiResponse<OrderResponse> startPreparation(@PathVariable Long orderId) {
		return ApiResponse.success("Order moved to preparation successfully", orderService.startPreparation(orderId));
	}

	@PatchMapping("/{orderId}/ready")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','KITCHEN_STAFF')")
	public ApiResponse<OrderResponse> readyOrder(@PathVariable Long orderId) {
		return ApiResponse.success("Order marked ready successfully", orderService.readyOrder(orderId));
	}

	@PatchMapping("/{orderId}/complete")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','CASHIER')")
	public ApiResponse<OrderResponse> completeOrder(@PathVariable Long orderId) {
		return ApiResponse.success("Order completed successfully", orderService.completeOrder(orderId));
	}

	@PatchMapping("/{orderId}/cancel")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','CASHIER')")
	public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long orderId) {
		return ApiResponse.success("Order canceled successfully", orderService.cancelOrder(orderId));
	}
}
