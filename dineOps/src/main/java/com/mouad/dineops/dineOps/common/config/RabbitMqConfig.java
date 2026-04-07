package com.mouad.dineops.dineOps.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	@Bean
	public TopicExchange eventsExchange(
			@Value("${app.messaging.events.exchange:dineops.events.exchange}") String exchangeName) {
		return new TopicExchange(exchangeName, true, false);
	}

	@Bean
	public Queue orderConfirmedQueue(
			@Value("${app.messaging.events.order-confirmed.queue:dineops.events.order-confirmed.queue}") String queueName) {
		return new Queue(queueName, true);
	}

	@Bean
	public Queue reservationApprovedQueue(
			@Value("${app.messaging.events.reservation-approved.queue:dineops.events.reservation-approved.queue}") String queueName) {
		return new Queue(queueName, true);
	}

	@Bean
	public Queue lowStockDetectedQueue(
			@Value("${app.messaging.events.low-stock-detected.queue:dineops.events.low-stock-detected.queue}") String queueName) {
		return new Queue(queueName, true);
	}

	@Bean
	public Binding orderConfirmedBinding(
			Queue orderConfirmedQueue,
			TopicExchange eventsExchange,
			@Value("${app.messaging.events.order-confirmed.routing-key:dineops.event.order.confirmed}") String routingKey) {
		return BindingBuilder
				.bind(orderConfirmedQueue)
				.to(eventsExchange)
				.with(routingKey);
	}

	@Bean
	public Binding reservationApprovedBinding(
			Queue reservationApprovedQueue,
			TopicExchange eventsExchange,
			@Value("${app.messaging.events.reservation-approved.routing-key:dineops.event.reservation.approved}") String routingKey) {
		return BindingBuilder
				.bind(reservationApprovedQueue)
				.to(eventsExchange)
				.with(routingKey);
	}

	@Bean
	public Binding lowStockDetectedBinding(
			Queue lowStockDetectedQueue,
			TopicExchange eventsExchange,
			@Value("${app.messaging.events.low-stock-detected.routing-key:dineops.event.low-stock.detected}") String routingKey) {
		return BindingBuilder
				.bind(lowStockDetectedQueue)
				.to(eventsExchange)
				.with(routingKey);
	}

	@Bean
	public MessageConverter rabbitMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
