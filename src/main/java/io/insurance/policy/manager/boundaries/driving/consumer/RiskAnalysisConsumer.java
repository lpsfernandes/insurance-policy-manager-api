package io.insurance.policy.manager.boundaries.driving.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.application.service.interfaces.IRiskAnalysisService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.ProcessingEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class RiskAnalysisConsumer {

    private final ObjectMapper objectMapper;
    private final IRiskAnalysisService riskAnalysisService;
    private final BusinessMetricsCollector metricsCollector;

    @KafkaListener(topics = "${spring.kafka.consumer.topics.risk-analysis-processing}",
            concurrency = "${spring.kafka.listener.concurrency:2}",
            containerFactory = "kafkaListenerContainerFactory")
    protected void consumeMessage(@Headers Map<String, Object> header, @Payload String message) {
        try {
            if (!message.isEmpty()) {
                var event = this.objectMapper.readValue(message, ProcessingEvent.class);
                this.riskAnalysisService.analyzeRisk((String) header.getOrDefault("traceparent", Strings.EMPTY),
                        event.policyId());
                this.metricsCollector.incrementKafkaEventsRiskAnalysConsumed();
            }
        } catch (JsonProcessingException e){
            log.error("Mensagem nao pode ser lida corretamente: {} - motivo {}. Evento descartado ", message, e.getMessage(), e);
        } catch (Exception e){
            log.error("Erro {} ao processar mensagem: [{}] ", e.getMessage(), message, e);
            throw e;
        }
    }

}
