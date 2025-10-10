package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProcessPolicyPaymentTests {

    @Mock
    private ISearchPolicyService searchPolicyService;

    @Mock
    private IPolicyStatusHandler policyStatusHandler;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    @InjectMocks
    private ProcessPolicyPayment processPolicyPayment;

    private Policy policy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        policy = new Policy();
        policy.setId("POL123");
        policy.setSubscriptionDate(ZonedDateTime.now().minusDays(1));
        policy.setCreatedAt(ZonedDateTime.now().minusHours(2));
    }

    @Test
    void shouldProcessAuthorizedPayment() {
        PaymentEvent event = new PaymentEvent("evt-001", "POL123", ZonedDateTime.now(), null, PaymentEvent.Status.APPROVED);

        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(policy));

        processPolicyPayment.process(event);

        verify(policyStatusHandler).processingStatusHandler(eq(policy), eq(Status.APPROVED), eq(ProcessingStatus.COMPLETED));
        verify(metricsCollector).incrementkafkaEventsPaymentApproved();
    }

    @Test
    void shouldProcessUnauthorizedPayment() {
        PaymentEvent event = new PaymentEvent("evt-002", "POL123", ZonedDateTime.now(), "Cartão recusado", PaymentEvent.Status.REJECTED);

        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(policy));

        processPolicyPayment.process(event);

        verify(policyStatusHandler).processingStatusHandler(eq(policy), eq(Status.REJECTED), eq(ProcessingStatus.COMPLETED));
        verify(metricsCollector).incrementkafkaEventsPaymentRejected();
        assertEquals("Cartão recusado", policy.getReason());
    }

    @Test
    void shouldDiscardEventIfPolicyNotFound() {
        PaymentEvent event = new PaymentEvent("evt-003", "POL404", ZonedDateTime.now(), null, PaymentEvent.Status.REJECTED);

        when(searchPolicyService.getPolicyById("POL404")).thenReturn(Optional.empty());

        processPolicyPayment.process(event);

        verifyNoInteractions(policyStatusHandler);
        verify(metricsCollector, never()).incrementkafkaEventsPaymentApproved();
        verify(metricsCollector, never()).incrementkafkaEventsPaymentRejected();
    }
}

