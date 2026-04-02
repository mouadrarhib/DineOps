package com.mouad.dineops.dineOps.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderItemRequest(
		@NotNull Long menuItemId,
		@NotNull @Min(1) Integer quantity,
		@Size(max = 255) String notes) {
}
