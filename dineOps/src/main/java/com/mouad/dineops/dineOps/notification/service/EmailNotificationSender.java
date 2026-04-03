package com.mouad.dineops.dineOps.notification.service;

public interface EmailNotificationSender {

	void send(String to, String subject, String content);
}
