package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessApolicyServiceTests {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private ProcessApolicyService serviceSpy;

    @BeforeEach
    void setUp() {
         var service = new ProcessApolicyService(policyRepository, metricsCollector);
         serviceSpy = Mockito.spy(service);
    }

    @Test
    void testProcessPaymentApproved() {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-001");
        policy.setSubscriptionDate(ZonedDateTime.now(ZoneId.of("UTC")));
        policy.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(1));

        PaymentEvent event = new PaymentEvent("evt-001", "policy-001", ZonedDateTime.now(ZoneId.of("UTC")), null, PaymentEvent.Status.APPROVED);

        when(policyRepository.findById("policy-001")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        serviceSpy.process(event);

        assertEquals(Status.APPROVED, policy.getStatus());
        verify(metricsCollector).incrementkafkaEventsPaymentApproved();
        verify(metricsCollector).incrementPoliciesApproved();
        verify(policyRepository).save(policy);
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
    }

    @Test
    void testProcessPaymentRejected() {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-002");
        policy.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(1));

        PaymentEvent event = new PaymentEvent("evt-002", "policy-002", ZonedDateTime.now(ZoneId.of("UTC")), "cartão recusado", PaymentEvent.Status.REJECTED);

        when(policyRepository.findById("policy-002")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        serviceSpy.process(event);

        assertEquals(Status.REJECTED, policy.getStatus());
        assertEquals("cartão recusado", policy.getReason());
        verify(metricsCollector).incrementkafkaEventsPaymentRejected();
        verify(metricsCollector).incrementPoliciesRejected();
        verify(policyRepository).save(policy);
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
    }

    @Test
    void testProcessPaymentPolicyNotFound() {
        PaymentEvent event = new PaymentEvent("evt-003", "policy-404", ZonedDateTime.now(ZoneId.of("UTC")), null, PaymentEvent.Status.APPROVED);

        when(policyRepository.findById("policy-404")).thenReturn(Optional.empty());

        serviceSpy.process(event);

        verify(policyRepository, never()).save(any());
        verifyNoInteractions(metricsCollector);
    }

    @Test
    void testProcessSubscriptionApproved() {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-010");
        policy.setPaymentDate(ZonedDateTime.now(ZoneId.of("UTC")));
        policy.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(1));

        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent("evt-010", "policy-010", ZonedDateTime.now(ZoneId.of("UTC")), null, InsuranceSubscriptionEvent.Status.APPROVED);

        when(policyRepository.findById("policy-010")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        serviceSpy.process(event);

        assertEquals(Status.APPROVED, policy.getStatus());
        verify(metricsCollector).incrementkafkaEventsSubscriptionApproved();
        verify(metricsCollector).incrementPoliciesApproved();
        verify(policyRepository).save(policy);
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
    }

    @Test
    void testProcessSubscriptionRejected() {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-011");
        policy.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(1));

        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent("evt-011", "policy-011", ZonedDateTime.now(ZoneId.of("UTC")), "dados inválidos", InsuranceSubscriptionEvent.Status.REJECTED);

        when(policyRepository.findById("policy-011")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        serviceSpy.process(event);

        assertEquals(Status.REJECTED, policy.getStatus());
        assertEquals("dados inválidos", policy.getReason());
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
        verify(metricsCollector).incrementkafkaEventsSubscriptionRejected();
        verify(metricsCollector).incrementPoliciesRejected();
        verify(policyRepository).save(policy);
    }

    @Test
    void testProcessSubscriptionPolicyNotFound() {
        InsuranceSubscriptionEvent event = new InsuranceSubscriptionEvent("evt-012", "policy-404", ZonedDateTime.now(ZoneId.of("UTC")), null, InsuranceSubscriptionEvent.Status.APPROVED);

        when(policyRepository.findById("policy-404")).thenReturn(Optional.empty());

        serviceSpy.process(event);

        verify(policyRepository, never()).save(any());
        verifyNoInteractions(metricsCollector);
    }

}
