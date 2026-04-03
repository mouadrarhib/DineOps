package com.mouad.dineops.dineOps.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.common.enums.NotificationStatus;
import com.mouad.dineops.dineOps.common.enums.NotificationType;
import com.mouad.dineops.dineOps.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);

	List<Notification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);

	boolean existsByTypeAndRelatedEntityTypeAndRelatedEntityId(
			NotificationType type,
			String relatedEntityType,
			Long relatedEntityId);
}
