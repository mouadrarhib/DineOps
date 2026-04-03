package com.mouad.dineops.dineOps.notification.dto;

import java.time.Instant;

import com.mouad.dineops.dineOps.common.enums.NotificationStatus;
import com.mouad.dineops.dineOps.common.enums.NotificationType;

public record NotificationResponse(
		Long id,
		NotificationType type,
		String recipient,
		String subject,
		String content,
		NotificationStatus status,
		String relatedEntityType,
		Long relatedEntityId,
		Instant sentAt,
		Instant createdAt,
		Instant updatedAt) {
}
