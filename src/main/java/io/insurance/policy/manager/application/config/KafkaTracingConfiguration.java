package io.insurance.policy.manager.application.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaTracingConfiguration {

    @Value("${spring.kafka.consumer.retry.interval:100}")
    private Long interval;
    @Value("${spring.kafka.consumer.retry.maxAttempts:3}")
    private Integer maxAttempts;


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setConsumerFactory(consumerFactory);
        var errorHandler = errorHandler();
        errorHandler.addNotRetryableExceptions(JsonProcessingException.class);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        var kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setObservationEnabled(true);
        return kafkaTemplate;
    }

    public DefaultErrorHandler errorHandler() {
        BackOff fixedBackOff = new FixedBackOff(this.interval, this.maxAttempts);
        return new DefaultErrorHandler((consumerRecord, exception) -> {
            log.error("Erro {} ao consumir mensagem kafka do topico {}, payload: {}", exception.getMessage(), consumerRecord.topic(), consumerRecord.value());
        }, fixedBackOff);
    }
}
