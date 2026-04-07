package com.mouad.dineops.dineOps.common.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqEventPublisher implements EventPublisher {

	private static final Logger log = LoggerFactory.getLogger(RabbitMqEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;
	private final boolean enabled;
	private final String exchangeName;

	public RabbitMqEventPublisher(
			RabbitTemplate rabbitTemplate,
			@Value("${app.messaging.rabbit.enabled:false}") boolean enabled,
			@Value("${app.messaging.events.exchange:dineops.events.exchange}") String exchangeName) {
		this.rabbitTemplate = rabbitTemplate;
		this.enabled = enabled;
		this.exchangeName = exchangeName;
	}

	@Override
	public void publish(String routingKey, Object payload) {
		if (!enabled) {
			log.debug("RabbitMQ disabled, skipping event publish for routing key {}", routingKey);
			return;
		}
		rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);
	}
}
