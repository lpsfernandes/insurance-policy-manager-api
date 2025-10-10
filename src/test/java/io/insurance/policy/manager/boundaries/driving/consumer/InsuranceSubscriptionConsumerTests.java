package io.insurance.policy.manager.boundaries.driving.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.application.service.interfaces.IProcessInsuranceSubscriptionPayment;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InsuranceSubscriptionConsumerTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IProcessInsuranceSubscriptionPayment processApolicyService;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    @InjectMocks
    private InsuranceSubscriptionConsumer consumer;

    @Captor
    private ArgumentCaptor<InsuranceSubscriptionEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldProcessValidMessage() throws Exception {
        InsuranceSubscriptionEvent event = getMockEvents();
        String message = "{\"orderId\":\"123\"}";

        when(objectMapper.readValue(message, InsuranceSubscriptionEvent.class)).thenReturn(event);

        consumer.consumeMessage(message);

        verify(objectMapper).readValue(message, InsuranceSubscriptionEvent.class);
        verify(processApolicyService).process(eventCaptor.capture());
        verify(metricsCollector).incrementKafkaSubscriptionEventConsumed();

        assertEquals(event, eventCaptor.getValue());
    }

    @Test
    void shouldLogErrorOnJsonProcessingException() throws Exception {
        String message = "{\"invalid\":true}";

        when(objectMapper.readValue(message, InsuranceSubscriptionEvent.class))
                .thenThrow(new JsonProcessingException("Erro de parsing") {});

        assertDoesNotThrow(() -> consumer.consumeMessage(message));

        verify(processApolicyService, never()).process(any());
        verify(metricsCollector, never()).incrementKafkaSubscriptionEventConsumed();
    }

    @Test
    void shouldThrowExceptionOnGenericError() throws Exception {
        InsuranceSubscriptionEvent event = getMockEvents();
        String message = "{\"orderId\":\"123\"}";

        when(objectMapper.readValue(message, InsuranceSubscriptionEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Erro inesperado")).when(processApolicyService).process(event);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> consumer.consumeMessage(message));
        assertEquals("Erro inesperado", thrown.getMessage());

        verify(metricsCollector, never()).incrementKafkaSubscriptionEventConsumed();
    }

    @Test
    void shouldIgnoreEmptyMessage() {
        consumer.consumeMessage("");

        verifyNoInteractions(objectMapper, processApolicyService, metricsCollector);
    }

    InsuranceSubscriptionEvent getMockEvents(){
        return new InsuranceSubscriptionEvent(
                "evt-001",
                "order-123",
                ZonedDateTime.parse("2025-10-09T22:00:00-03:00"),
                "Subscrição aprovado",
                InsuranceSubscriptionEvent.Status.APPROVED
        );

    }
}