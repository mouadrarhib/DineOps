package com.mouad.dineops.dineOps.common.response;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
		return ResponseEntity.ok(
				ApiResponse.success(
						"Service is healthy",
						Map.of(
								"status", "UP",
								"service", "dineOps",
								"timestamp", Instant.now().toString())));
	}
}
