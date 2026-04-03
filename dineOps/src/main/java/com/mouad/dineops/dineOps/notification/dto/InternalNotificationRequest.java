package com.mouad.dineops.dineOps.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InternalNotificationRequest(
		@NotBlank @Size(max = 150) String recipient,
		@Size(max = 255) String subject,
		@NotBlank String content,
		@Size(max = 50) String relatedEntityType,
		Long relatedEntityId) {
}
