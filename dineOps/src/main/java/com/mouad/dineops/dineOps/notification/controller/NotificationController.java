package com.mouad.dineops.dineOps.notification.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.notification.dto.InternalNotificationRequest;
import com.mouad.dineops.dineOps.notification.dto.NotificationResponse;
import com.mouad.dineops.dineOps.notification.dto.TestEmailRequest;
import com.mouad.dineops.dineOps.notification.service.NotificationService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@PostMapping("/internal")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<NotificationResponse> createInternalNotification(
			@Valid @RequestBody InternalNotificationRequest request) {
		return ApiResponse.success(
				"Internal notification created successfully",
				notificationService.createInternalNotification(request));
	}

	@PostMapping("/test-email")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<NotificationResponse> sendTestEmail(@Valid @RequestBody TestEmailRequest request) {
		return ApiResponse.success("Test email notification sent", notificationService.sendTestEmail(request));
	}
}
