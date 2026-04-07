package com.mouad.dineops.dineOps.common.messaging;

public interface EventPublisher {

	void publish(String routingKey, Object payload);
}
