package com.mouad.dineops.dineOps.common.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditMetadataFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = resolveRequestId(request);
		String userId = resolveUserId();
		String ipAddress = resolveIpAddress(request);
		String userAgent = request.getHeader("User-Agent");
		String requestPath = request.getRequestURI();
		String httpMethod = request.getMethod();

		AuditMetadata metadata = new AuditMetadata(requestId, userId, ipAddress, userAgent, requestPath, httpMethod);
		AuditMetadataContext.set(metadata);
		MDC.put("requestId", requestId);
		MDC.put("userId", userId);
		response.setHeader("X-Request-Id", requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			AuditMetadataContext.clear();
			MDC.remove("requestId");
			MDC.remove("userId");
		}
	}

	private String resolveRequestId(HttpServletRequest request) {
		String headerValue = request.getHeader("X-Request-Id");
		return headerValue == null || headerValue.isBlank() ? UUID.randomUUID().toString() : headerValue;
	}

	private String resolveUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return "anonymous";
		}
		return authentication.getName();
	}

	private String resolveIpAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
