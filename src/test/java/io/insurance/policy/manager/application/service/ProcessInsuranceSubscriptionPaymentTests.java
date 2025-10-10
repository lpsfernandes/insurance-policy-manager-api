package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProcessInsuranceSubscriptionPaymentTest {

    @Mock
    private ISearchPolicyService searchPolicyService;

    @Mock
    private IPolicyStatusHandler policyStatusHandler;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    @InjectMocks
    private ProcessInsuranceSubscriptionPayment processInsuranceSubscriptionPayment;

    private Policy policy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        policy = new Policy();
        policy.setId("POL123");
        policy.setCreatedAt(ZonedDateTime.now().minusHours(2));
        policy.setPaymentDate(ZonedDateTime.now().minusDays(1));
    }

    @Test
    void shouldProcessAuthorizedSubscription() {
        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent(
                "evt-001", "POL123", ZonedDateTime.now(), null, InsuranceSubscriptionEvent.Status.APPROVED);

        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(policy));

        processInsuranceSubscriptionPayment.process(event);

        verify(policyStatusHandler).processingStatusHandler(policy, Status.APPROVED, ProcessingStatus.COMPLETED);
        verify(metricsCollector).incrementkafkaEventsSubscriptionApproved();
        assertEquals(event.subscriptionDateTime(), policy.getSubscriptionDate());
    }

    @Test
    void shouldProcessUnauthorizedSubscription() {
        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent(
                "evt-002", "POL123", ZonedDateTime.now(), "Dados inválidos", InsuranceSubscriptionEvent.Status.REJECTED);

        when(searchPolicyService.getPolicyById("POL123")).thenReturn(Optional.of(policy));

        processInsuranceSubscriptionPayment.process(event);

        verify(policyStatusHandler).processingStatusHandler(policy, Status.REJECTED, ProcessingStatus.COMPLETED);
        verify(metricsCollector).incrementkafkaEventsSubscriptionRejected();
        assertEquals("Dados inválidos", policy.getReason());
    }

    @Test
    void shouldDiscardEventIfPolicyNotFound() {
        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent(
                "evt-003", "POL404", ZonedDateTime.now(), null, InsuranceSubscriptionEvent.Status.REJECTED);

        when(searchPolicyService.getPolicyById("POL404")).thenReturn(Optional.empty());

        processInsuranceSubscriptionPayment.process(event);

        verifyNoInteractions(policyStatusHandler);
        verify(metricsCollector, never()).incrementkafkaEventsSubscriptionApproved();
        verify(metricsCollector, never()).incrementkafkaEventsSubscriptionRejected();
    }
}
