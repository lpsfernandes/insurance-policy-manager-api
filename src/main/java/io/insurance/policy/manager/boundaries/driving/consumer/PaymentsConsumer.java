package io.insurance.policy.manager.boundaries.driving.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.application.service.interfaces.IProcessApolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@AllArgsConstructor
public class PaymentsConsumer {

    private final ObjectMapper objectMapper;
    private final IProcessApolicyService processApolicyService;
    private final BusinessMetricsCollector metricsCollector;

    @KafkaListener(topics = "${spring.kafka.consumer.topics.payments}",
            concurrency = "${spring.kafka.listener.concurrency:1}",
            containerFactory = "kafkaListenerContainerFactory")
    protected void consumeMessage(@Payload String message) {
        try {
            if (!message.isEmpty()) {
                var event = this.objectMapper.readValue(message, PaymentEvent.class);
                this.processApolicyService.process(event);
                metricsCollector.incrementKafkaPaymentEventConsumed();
            }
        } catch (JsonProcessingException e){
            log.error("Mensagem nao pode ser lida corretamente: {} - motivo {}. Evento descartado ", message, e.getMessage(), e);
        } catch (Exception e){
            log.error("Erro {} ao processar mensagem: [{}] ", e.getMessage(), message, e);
            throw e;
        }
    }

}
