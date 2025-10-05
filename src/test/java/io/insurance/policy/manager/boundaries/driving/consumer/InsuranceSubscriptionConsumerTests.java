package io.insurance.policy.manager.boundaries.driving.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.service.BusinessMetricsCollector;

import io.insurance.policy.manager.application.service.interfaces.IProcessApolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InsuranceSubscriptionConsumerTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IProcessApolicyService processApolicyService;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private InsuranceSubscriptionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InsuranceSubscriptionConsumer(objectMapper, processApolicyService, metricsCollector);
    }

    @Test
    void testConsumeMessageWithValidPayload() throws Exception {
        String message = "{\"policyId\":\"123\"}";
        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent(
                "event-001",
                "order-123",
                ZonedDateTime.now(ZoneId.of("UTC")),
                null,
                InsuranceSubscriptionEvent.Status.APPROVED
        );

        when(objectMapper.readValue(message, InsuranceSubscriptionEvent.class)).thenReturn(event);

        consumer.consumeMessage(message);

        verify(objectMapper).readValue(message, InsuranceSubscriptionEvent.class);
        verify(processApolicyService).process(event);
        verify(metricsCollector).incrementKafkaSubscriptionEventConsumed();
    }

    @Test
    void testConsumeMessageWithEmptyPayload() {
        consumer.consumeMessage("");

        verifyNoInteractions(objectMapper, processApolicyService, metricsCollector);
    }

    @Test
    void testConsumeMessageWithInvalidJson() throws Exception {
        String message = "invalid-json";

        when(objectMapper.readValue(message, InsuranceSubscriptionEvent.class))
                .thenThrow(new JsonProcessingException("Erro de parsing") {});

        consumer.consumeMessage(message);

        verify(objectMapper).readValue(message, InsuranceSubscriptionEvent.class);
        verifyNoInteractions(processApolicyService);
        verifyNoInteractions(metricsCollector);
    }

    @Test
    void testConsumeMessageWithUnexpectedException() throws Exception {
        String message = "{\"policyId\":\"123\"}";

        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent(
                "event-001",
                "order-123",
                ZonedDateTime.now(ZoneId.of("UTC")),
                null,
                InsuranceSubscriptionEvent.Status.APPROVED
        );


        when(objectMapper.readValue(message, InsuranceSubscriptionEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Erro interno")).when(processApolicyService).process(event);

        try {
            consumer.consumeMessage(message);
        } catch (RuntimeException e) {
            // esperado
        }

        verify(objectMapper).readValue(message, InsuranceSubscriptionEvent.class);
        verify(processApolicyService).process(event);
        verify(metricsCollector, never()).incrementKafkaSubscriptionEventConsumed();
    }
}
