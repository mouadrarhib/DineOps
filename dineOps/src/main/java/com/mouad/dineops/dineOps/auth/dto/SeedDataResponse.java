package com.mouad.dineops.dineOps.auth.dto;

import java.util.List;

public record SeedDataResponse(
		List<String> roles,
		List<String> permissions,
		List<String> users) {
}
