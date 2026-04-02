package com.mouad.dineops.dineOps.order.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
		@NotNull Long branchId,
		@NotBlank @Size(max = 30) String source,
		@DecimalMin(value = "0.0") BigDecimal taxRatePercent,
		@DecimalMin(value = "0.0") BigDecimal taxAmount,
		@Size(max = 255) String notes,
		@NotEmpty List<@Valid CreateOrderItemRequest> items) {
}
