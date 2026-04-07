package com.mouad.dineops.dineOps.auth.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

	private final JwtTokenService jwtTokenService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService, CustomUserDetailsService userDetailsService) {
		this.jwtTokenService = jwtTokenService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String jwt = extractAccessToken(request);
		if (jwt == null || jwt.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		boolean tokenValid;
		try {
			tokenValid = jwtTokenService.isTokenValid(jwt, "access");
		} catch (Exception ignored) {
			tokenValid = false;
		}

		if (!tokenValid) {
			filterChain.doFilter(request, response);
			return;
		}

		String subject = jwtTokenService.extractSubject(jwt);
		if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					userDetails,
					null,
					userDetails.getAuthorities());
			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}

		filterChain.doFilter(request, response);
	}

	private String extractAccessToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		return null;
	}
}
