package io.insurance.policy.manager.boundaries.driving.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.service.BusinessMetricsCollector;

import io.insurance.policy.manager.application.service.interfaces.IProcessApolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentsConsumerTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IProcessApolicyService processApolicyService;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private PaymentsConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentsConsumer(objectMapper, processApolicyService, metricsCollector);
    }

    @Test
    void testConsumeMessageWithValidPayload() throws Exception {
        String message = "{\"paymentId\":\"123\"}";
        PaymentEvent event = new PaymentEvent(
                "payment-001",
                "order-123",
                ZonedDateTime.now(ZoneId.of("UTC")),
                null,
                PaymentEvent.Status.APPROVED
        );


        when(objectMapper.readValue(message, PaymentEvent.class)).thenReturn(event);

        consumer.consumeMessage(message);

        verify(objectMapper).readValue(message, PaymentEvent.class);
        verify(processApolicyService).process(event);
        verify(metricsCollector).incrementKafkaPaymentEventConsumed();
    }

    @Test
    void testConsumeMessageWithEmptyPayload() {
        consumer.consumeMessage("");

        verifyNoInteractions(objectMapper, processApolicyService, metricsCollector);
    }

    @Test
    void testConsumeMessageWithInvalidJson() throws Exception {
        String message = "invalid-json";

        when(objectMapper.readValue(message, PaymentEvent.class))
                .thenThrow(new JsonProcessingException("Erro de parsing") {});

        consumer.consumeMessage(message);

        verify(objectMapper).readValue(message, PaymentEvent.class);
        verifyNoInteractions(processApolicyService);
        verifyNoInteractions(metricsCollector);
    }

    @Test
    void testConsumeMessageWithUnexpectedException() throws Exception {
        String message = "{\"paymentId\":\"123\"}";
        PaymentEvent event = new PaymentEvent(
                "payment-001",
                "order-123",
                ZonedDateTime.now(ZoneId.of("UTC")),
                null,
                PaymentEvent.Status.APPROVED
        );


        when(objectMapper.readValue(message, PaymentEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Erro interno")).when(processApolicyService).process(event);

        try {
            consumer.consumeMessage(message);
        } catch (RuntimeException e) {
            // esperado
        }

        verify(objectMapper).readValue(message, PaymentEvent.class);
        verify(processApolicyService).process(event);
        verify(metricsCollector, never()).incrementKafkaPaymentEventConsumed();
    }
}
