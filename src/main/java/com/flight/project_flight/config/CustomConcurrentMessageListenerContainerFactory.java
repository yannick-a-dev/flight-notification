package com.flight.project_flight.config;

import org.springframework.kafka.core.ConsumerFactory;

public class CustomConcurrentMessageListenerContainerFactory<T, T1> {
    private ConsumerFactory<T, T1> consumerFactory;

    public void setConsumerFactory(ConsumerFactory<T, T1> consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    public ConsumerFactory<T, T1> getConsumerFactory() {
        return consumerFactory;
    }
}
