package com.mouad.dineops.dineOps.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import com.mouad.dineops.dineOps.common.security.AuditMetadataFilter;
import com.mouad.dineops.dineOps.common.security.RequestLoggingFilter;
import com.mouad.dineops.dineOps.auth.security.CustomUserDetailsService;
import com.mouad.dineops.dineOps.auth.security.JwtAccessDeniedHandler;
import com.mouad.dineops.dineOps.auth.security.JwtAuthenticationEntryPoint;
import com.mouad.dineops.dineOps.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${app.security.cors.allowed-origins:*}")
	private List<String> allowedOrigins;

	private static final String[] PUBLIC_ENDPOINTS = {
			"/api/auth/login",
			"/api/auth/refresh",
			"/api/auth/logout",
			"/api/auth/seed",
			"/api/health",
			"/api/health/**",
			"/actuator/health",
			"/actuator/health/**",
			"/actuator/info",
			"/actuator/metrics",
			"/actuator/metrics/**",
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html"
	};

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			AuditMetadataFilter auditMetadataFilter,
			RequestLoggingFilter requestLoggingFilter,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
 			JwtAccessDeniedHandler jwtAccessDeniedHandler,
			DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(daoAuthenticationProvider)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
						.accessDeniedHandler(jwtAccessDeniedHandler))
				.addFilterBefore(auditMetadataFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(requestLoggingFilter, AuditMetadataFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().authenticated());

		return http.build();
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider(
			CustomUserDetailsService customUserDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
