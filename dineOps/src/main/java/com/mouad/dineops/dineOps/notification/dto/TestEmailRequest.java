package com.mouad.dineops.dineOps.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestEmailRequest(
		@Email @NotBlank @Size(max = 150) String to,
		@NotBlank @Size(max = 255) String subject,
		@NotBlank String content) {
}
