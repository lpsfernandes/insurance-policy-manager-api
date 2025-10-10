package io.insurance.policy.manager.boundaries.driving.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.application.service.interfaces.IProcessPolicyPayment;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentsConsumerTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IProcessPolicyPayment processPolicyPayment;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    @InjectMocks
    private PaymentsConsumer paymentsConsumer;

    @Captor
    private ArgumentCaptor<PaymentEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessValidMessage() throws Exception {
        String message = "{\"orderId\":\"123\"}";
        PaymentEvent event = getMockEvents();

        when(objectMapper.readValue(message, PaymentEvent.class)).thenReturn(event);

        paymentsConsumer.consumeMessage(message);

        verify(objectMapper).readValue(message, PaymentEvent.class);
        verify(processPolicyPayment).process(eventCaptor.capture());
        verify(metricsCollector).incrementKafkaPaymentEventConsumed();

        assertEquals(event, eventCaptor.getValue());
    }

    @Test
    void shouldLogErrorOnJsonProcessingException() throws Exception {
        String message = "{\"invalid\":true}";

        when(objectMapper.readValue(message, PaymentEvent.class)).thenThrow(new JsonProcessingException("Erro de parsing") {});

        assertDoesNotThrow(() -> paymentsConsumer.consumeMessage(message));

        verify(processPolicyPayment, never()).process(any());
        verify(metricsCollector, never()).incrementKafkaPaymentEventConsumed();
    }

    @Test
    void shouldThrowExceptionOnGenericError() throws Exception {
        String message = "{\"orderId\":\"123\"}";
        PaymentEvent event = getMockEvents();

        when(objectMapper.readValue(message, PaymentEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Erro inesperado")).when(processPolicyPayment).process(event);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> paymentsConsumer.consumeMessage(message));
        assertEquals("Erro inesperado", thrown.getMessage());

        verify(metricsCollector, never()).incrementKafkaPaymentEventConsumed();
    }

    @Test
    void shouldIgnoreEmptyMessage() {
        paymentsConsumer.consumeMessage("");

        verifyNoInteractions(objectMapper, processPolicyPayment, metricsCollector);
    }

    PaymentEvent getMockEvents(){
        return new PaymentEvent(
                "evt-001",
                "order-123",
                ZonedDateTime.parse("2025-10-09T22:00:00-03:00"),
                "Pagamento aprovado",
                PaymentEvent.Status.APPROVED
        );

    }

}
