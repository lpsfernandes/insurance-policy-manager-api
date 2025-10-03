package io.manager.policy.application.config;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaListenerConfig {

    @Value("${spring.kafka.consumer.retry.interval:100}")
    private long interval;
    @Value("${spring.kafka.consumer.retry.maxAttempts:3}")
    private Integer maxAttempts;

    @Bean
    public DefaultErrorHandler errorHandler() {
        BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
        return new DefaultErrorHandler((consumerRecord, exception) -> {
            log.error("Erro {} ao consumir mensagem kafka do topico {}, payload: {}", exception.getMessage(), consumerRecord.topic(), consumerRecord.value());
        }, fixedBackOff);
    }


}
