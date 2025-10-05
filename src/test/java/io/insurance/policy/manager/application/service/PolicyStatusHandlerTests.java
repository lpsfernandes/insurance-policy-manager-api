package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.domain.exception.StatusNotAllowed;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyStatusHandlerTests {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private PolicyStatusHandler handlerSpy;

    @BeforeEach
    void setUp() {
        var handler = new PolicyStatusHandler(policyRepository, metricsCollector);
        handlerSpy =  Mockito.spy(handler);
    }

    @Test
    void testStatusHandlerWithApprovedStatus() {
        doNothing().when(handlerSpy).insertOutboxEvent(any(), any());
        doNothing().when(handlerSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-001");
        policy.setStatus(Status.PENDING);

        when(policyRepository.findById("policy-001")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        Optional<Policy> result = handlerSpy.statusHandler("policy-001", Status.APPROVED);

        assertTrue(result.isPresent());
        assertEquals(Status.APPROVED, result.get().getStatus());

        verify(policyRepository).save(policy);
        verify(metricsCollector).incrementPoliciesApproved();
        verify(metricsCollector, never()).incrementPoliciesRejected();
        verify(handlerSpy).insertOutboxEvent(any(), any());
        verify(handlerSpy).insertStatusHistory(any(), any());
    }

    @Test
    void testStatusHandlerWithRejectedStatus() {
        doNothing().when(handlerSpy).insertOutboxEvent(any(), any());
        doNothing().when(handlerSpy).insertStatusHistory(any(), any());

        Policy policy = new Policy();
        policy.setId("policy-002");
        policy.setStatus(Status.VALIDATED);

        when(policyRepository.findById("policy-002")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenReturn(policy);

        Optional<Policy> result = handlerSpy.statusHandler("policy-002", Status.REJECTED);

        assertTrue(result.isPresent());
        assertEquals(Status.REJECTED, result.get().getStatus());

        verify(policyRepository).save(policy);
        verify(metricsCollector).incrementPoliciesRejected();
        verify(metricsCollector, never()).incrementPoliciesApproved();
        verify(handlerSpy).insertOutboxEvent(any(), any());
        verify(handlerSpy).insertStatusHistory(any(), any());
    }

    @Test
    void testStatusHandlerWithPolicyNotFound() {
        when(policyRepository.findById("policy-404")).thenReturn(Optional.empty());

        Optional<Policy> result = handlerSpy.statusHandler("policy-404", Status.APPROVED);

        assertTrue(result.isEmpty());

        verify(policyRepository, never()).save(any());
        verifyNoInteractions(metricsCollector);
    }

    @Test
    void testStatusHandlerThrowsExceptionOnSave() {
        Policy policy = new Policy();
        policy.setId("policy-003");
        policy.setStatus(Status.RECEIVED);

        when(policyRepository.findById("policy-003")).thenReturn(Optional.of(policy));

        assertThrows(StatusNotAllowed.class, () ->
                handlerSpy.statusHandler("policy-003", Status.APPROVED));

        verify(policyRepository).findById("policy-003");
        verifyNoInteractions(metricsCollector);
    }
}

