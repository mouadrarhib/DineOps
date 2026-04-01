package com.mouad.dineops.dineOps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DineOpsApplication {

	@Value("${server.port:8080}")
	private String serverPort;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(DineOpsApplication.class);
		String activeProfile = System.getProperty("spring.profiles.active");
		String activeProfileEnv = System.getenv("SPRING_PROFILES_ACTIVE");
		if ((activeProfile == null || activeProfile.isBlank())
				&& (activeProfileEnv == null || activeProfileEnv.isBlank())) {
			application.setAdditionalProfiles("local");
		}
		application.run(args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		System.out.println("*********************** DineOps is running successfully. ***********************");
		System.out.println("Swagger UI: http://localhost:" + serverPort + contextPath + "/swagger-ui.html");
	}

}
