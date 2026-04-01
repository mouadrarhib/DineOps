package com.mouad.dineops.dineOps.common.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		filterChain.doFilter(request, response);
		long duration = System.currentTimeMillis() - startTime;

		String requestId = AuditMetadataContext.get().map(AuditMetadata::requestId).orElse("n/a");
		String userId = AuditMetadataContext.get().map(AuditMetadata::userId).orElse("anonymous");

		LOGGER.info("requestId={} method={} path={} status={} durationMs={} userId={}",
				requestId,
				request.getMethod(),
				request.getRequestURI(),
				response.getStatus(),
				duration,
				userId);
	}
}
