package com.mouad.dineops.dineOps.auth.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String ACCESS_COOKIE = "ACCESS_TOKEN";
	private static final String REFRESH_COOKIE = "REFRESH_TOKEN";

	private final AuthService authService;

	@Value("${app.security.cookies.secure:false}")
	private boolean cookiesSecure;

	@Value("${app.security.cookies.same-site:Lax}")
	private String cookiesSameSite;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ApiResponse<AuthTokensResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletResponse httpServletResponse) {
		AuthTokensResponse response = authService.login(request.email(), request.password());
		setAuthCookies(httpServletResponse, response);
		return ApiResponse.success("Login successful", response);
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthTokensResponse> refresh(
			@Valid @RequestBody RefreshTokenRequest request,
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie,
			HttpServletResponse httpServletResponse) {
		String refreshToken = request.refreshToken() == null || request.refreshToken().isBlank()
				? refreshCookie
				: request.refreshToken();
		AuthTokensResponse response = authService.refresh(refreshToken);
		setAuthCookies(httpServletResponse, response);
		return ApiResponse.success("Token refreshed", response);
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(
			@Valid @RequestBody LogoutRequest request,
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie,
			HttpServletResponse httpServletResponse) {
		String refreshToken = request.refreshToken() == null || request.refreshToken().isBlank()
				? refreshCookie
				: request.refreshToken();
		authService.logout(refreshToken);
		clearAuthCookies(httpServletResponse);
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

	private void setAuthCookies(HttpServletResponse response, AuthTokensResponse tokens) {
		long accessSeconds = Math.max(0, Duration.between(Instant.now(), tokens.accessTokenExpiresAt()).getSeconds());
		long refreshSeconds = Math.max(0, Duration.between(Instant.now(), tokens.refreshTokenExpiresAt()).getSeconds());

		ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE, tokens.accessToken())
				.httpOnly(true)
				.secure(cookiesSecure)
				.path("/")
				.sameSite(cookiesSameSite)
				.maxAge(Duration.ofSeconds(accessSeconds))
				.build();

		ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, tokens.refreshToken())
				.httpOnly(true)
				.secure(cookiesSecure)
				.path("/")
				.sameSite(cookiesSameSite)
				.maxAge(Duration.ofSeconds(refreshSeconds))
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
	}

	private void clearAuthCookies(HttpServletResponse response) {
		ResponseCookie clearAccess = ResponseCookie.from(ACCESS_COOKIE, "")
				.httpOnly(true)
				.secure(cookiesSecure)
				.path("/")
				.sameSite(cookiesSameSite)
				.maxAge(Duration.ZERO)
				.build();

		ResponseCookie clearRefresh = ResponseCookie.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(cookiesSecure)
				.path("/")
				.sameSite(cookiesSameSite)
				.maxAge(Duration.ZERO)
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
	}
}
