package com.mouad.dineops.dineOps.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.auth.dto.AuthTokensResponse;
import com.mouad.dineops.dineOps.auth.dto.CurrentUserResponse;
import com.mouad.dineops.dineOps.auth.dto.LoginRequest;
import com.mouad.dineops.dineOps.auth.dto.LogoutRequest;
import com.mouad.dineops.dineOps.auth.dto.RefreshTokenRequest;
import com.mouad.dineops.dineOps.auth.dto.SeedDataResponse;
import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.auth.service.AuthService;
import com.mouad.dineops.dineOps.common.response.ApiResponse;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ApiResponse<AuthTokensResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthTokensResponse response = authService.login(request.email(), request.password());
		return ApiResponse.success("Login successful", response);
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthTokensResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		AuthTokensResponse response = authService.refresh(request.refreshToken());
		return ApiResponse.success("Token refreshed", response);
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request.refreshToken());
		return ApiResponse.success("Logged out successfully");
	}

	@PostMapping("/seed")
	public ApiResponse<SeedDataResponse> seed() {
		return ApiResponse.success("Virtual auth data seeded", authService.seedVirtualAuthData());
	}

	@GetMapping("/me")
	public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal AppUserPrincipal principal) {
		return ApiResponse.success("Current user fetched", authService.currentUser(principal));
	}
}
