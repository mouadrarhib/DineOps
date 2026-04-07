package com.mouad.dineops.dineOps.notification.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mouad.dineops.dineOps.notification.event.OrderConfirmedEvent;
import com.mouad.dineops.dineOps.notification.service.NotificationService;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
public class OrderConfirmedEventConsumer {

	private final NotificationService notificationService;

	public OrderConfirmedEventConsumer(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@RabbitListener(queues = "${app.messaging.events.order-confirmed.queue:dineops.events.order-confirmed.queue}")
	public void onOrderConfirmed(OrderConfirmedEvent event) {
		notificationService.sendInternal(
				"branch:" + event.branchId() + ":kitchen",
				"Order confirmed",
				"Order " + event.orderNumber() + " was confirmed with total " + event.totalAmount() + ".",
				"ORDER_CONFIRMED",
				event.orderId());
	}
}
