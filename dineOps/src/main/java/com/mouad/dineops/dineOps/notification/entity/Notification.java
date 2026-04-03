package com.mouad.dineops.dineOps.notification.entity;

import java.time.Instant;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;
import com.mouad.dineops.dineOps.common.enums.NotificationStatus;
import com.mouad.dineops.dineOps.common.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NotificationType type;

	@Column(nullable = false, length = 150)
	private String recipient;

	@Column(length = 255)
	private String subject;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NotificationStatus status;

	@Column(name = "related_entity_type", length = 50)
	private String relatedEntityType;

	@Column(name = "related_entity_id")
	private Long relatedEntityId;

	@Column(name = "sent_at")
	private Instant sentAt;
}
