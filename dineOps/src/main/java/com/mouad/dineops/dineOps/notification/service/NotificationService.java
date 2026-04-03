package com.mouad.dineops.dineOps.notification.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.common.enums.NotificationStatus;
import com.mouad.dineops.dineOps.common.enums.NotificationType;
import com.mouad.dineops.dineOps.notification.dto.InternalNotificationRequest;
import com.mouad.dineops.dineOps.notification.dto.NotificationResponse;
import com.mouad.dineops.dineOps.notification.dto.TestEmailRequest;
import com.mouad.dineops.dineOps.notification.entity.Notification;
import com.mouad.dineops.dineOps.notification.repository.NotificationRepository;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final EmailNotificationSender emailNotificationSender;

	public NotificationService(
			NotificationRepository notificationRepository,
			EmailNotificationSender emailNotificationSender) {
		this.notificationRepository = notificationRepository;
		this.emailNotificationSender = emailNotificationSender;
	}

	@Transactional
	public NotificationResponse createInternalNotification(InternalNotificationRequest request) {
		return sendInternal(
				request.recipient(),
				request.subject(),
				request.content(),
				request.relatedEntityType(),
				request.relatedEntityId());
	}

	@Transactional
	public NotificationResponse sendInternal(
			String recipient,
			String subject,
			String content,
			String relatedEntityType,
			Long relatedEntityId) {
		Notification notification = new Notification();
		notification.setType(NotificationType.IN_APP);
		notification.setRecipient(recipient.trim());
		notification.setSubject(trimOrNull(subject));
		notification.setContent(content.trim());
		notification.setStatus(NotificationStatus.SENT);
		notification.setRelatedEntityType(trimOrNull(relatedEntityType));
		notification.setRelatedEntityId(relatedEntityId);
		notification.setSentAt(Instant.now());

		return toResponse(notificationRepository.save(notification));
	}

	@Transactional
	public NotificationResponse sendTestEmail(TestEmailRequest request) {
		return sendEmail(
				request.to(),
				request.subject(),
				request.content(),
				null,
				null);
	}

	@Transactional
	public NotificationResponse sendEmail(
			String to,
			String subject,
			String content,
			String relatedEntityType,
			Long relatedEntityId) {
		Notification notification = new Notification();
		notification.setType(NotificationType.EMAIL);
		notification.setRecipient(to.trim().toLowerCase());
		notification.setSubject(subject.trim());
		notification.setContent(content.trim());
		notification.setStatus(NotificationStatus.PENDING);
		notification.setRelatedEntityType(trimOrNull(relatedEntityType));
		notification.setRelatedEntityId(relatedEntityId);

		Notification saved = notificationRepository.save(notification);
		try {
			emailNotificationSender.send(saved.getRecipient(), saved.getSubject(), saved.getContent());
			saved.setStatus(NotificationStatus.SENT);
			saved.setSentAt(Instant.now());
		} catch (Exception ex) {
			saved.setStatus(NotificationStatus.FAILED);
		}

		return toResponse(notificationRepository.save(saved));
	}

	private String trimOrNull(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	private NotificationResponse toResponse(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getType(),
				notification.getRecipient(),
				notification.getSubject(),
				notification.getContent(),
				notification.getStatus(),
				notification.getRelatedEntityType(),
				notification.getRelatedEntityId(),
				notification.getSentAt(),
				notification.getCreatedAt(),
				notification.getUpdatedAt());
	}
}
