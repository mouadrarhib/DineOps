package com.mouad.dineops.dineOps.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEmailNotificationSender implements EmailNotificationSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingEmailNotificationSender.class);

	@Override
	public void send(String to, String subject, String content) {
		log.info("Demo email sender: to='{}' subject='{}' content='{}'", to, subject, content);
	}
}
