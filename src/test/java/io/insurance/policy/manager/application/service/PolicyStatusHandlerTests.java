package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.domain.exception.StatusNotAllowed;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyStatusHandlerTests {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private PolicyStatusHandler serviceSpy;

    private Policy policy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var service = new PolicyStatusHandler(policyRepository, metricsCollector);
        serviceSpy = Mockito.spy(service);
        policy = new Policy();
        policy.setId("POL123");
        policy.setStatus(Status.PENDING);
        policy.setInsuredAmount(1L);
        policy.setMonthlyPremium(1L);
        policy.setProcessingStatus(ProcessingStatus.AWAITING_PAYMENT_AND_SUBSCRIPTION);
        policy.setHistory(Set.of());
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        when(policyRepository.findById("POL123")).thenReturn(Optional.of(policy));

        Optional<Policy> result = serviceSpy.statusHandler("POL123", Status.APPROVED);

        assertTrue(result.isPresent());
        assertEquals(Status.APPROVED, policy.getStatus());
        verify(policyRepository, times(2)).findById("POL123");
        verify(policyRepository).save(policy);
        verify(metricsCollector).incrementPoliciesApproved();
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
    }

    @Test
    void shouldNotUpdateStatusIfSame() {
        policy.setStatus(Status.APPROVED);
        when(policyRepository.findById("POL123")).thenReturn(Optional.of(policy));

        Optional<Policy> result = serviceSpy.statusHandler("POL123", Status.APPROVED);

        assertTrue(result.isPresent());
        verify(policyRepository, never()).save(any());
        verify(metricsCollector, never()).incrementPoliciesApproved();
    }

    @Test
    void shouldThrowStatusNotAllowedException() {
        policy.setStatus(Status.REJECTED);
        when(policyRepository.findById("POL123")).thenReturn(Optional.of(policy));

        assertThrows(StatusNotAllowed.class, () -> serviceSpy.statusHandler("POL123", Status.APPROVED));
    }

    @Test
    void shouldUpdateProcessingStatusSuccessfully() {
        serviceSpy.processingStatusHandler(policy, ProcessingStatus.COMPLETED);

        assertEquals(ProcessingStatus.COMPLETED, policy.getProcessingStatus());
        verify(policyRepository).save(policy);
    }

    @Test
    void shouldThrowProcessingStatusNotAllowedException() {
        policy.setProcessingStatus(ProcessingStatus.COMPLETED);

        assertThrows(ProcessingStatusNotAllowed.class, () ->
                serviceSpy.processingStatusHandler(policy, ProcessingStatus.AWAITING_PAYMENT_AND_SUBSCRIPTION));
    }

    @Test
    void shouldUpdateBothStatusAndProcessingStatus() {
        doNothing().when(serviceSpy).insertOutboxEvent(any(), any());
        doNothing().when(serviceSpy).insertStatusHistory(any(), any());

        policy.setStatus(Status.PENDING);
        policy.setProcessingStatus(ProcessingStatus.AWAITING_PAYMENT_AND_SUBSCRIPTION);

        serviceSpy.processingStatusHandler(policy, Status.REJECTED, ProcessingStatus.COMPLETED);

        assertEquals(Status.REJECTED, policy.getStatus());
        assertEquals(ProcessingStatus.COMPLETED, policy.getProcessingStatus());
        verify(policyRepository).save(policy);
        verify(metricsCollector).incrementPoliciesRejected();
        verify(serviceSpy).insertOutboxEvent(any(), any());
        verify(serviceSpy).insertStatusHistory(any(), any());
    }

    @Test
    void shouldThrowProcessingProcessingStatusNotAllowedException() {

        policy.setStatus(Status.PENDING);
        policy.setProcessingStatus(ProcessingStatus.AWAITING_RISK_ANALYSIS);

        assertThrows(ProcessingStatusNotAllowed.class, () ->
            serviceSpy.processingStatusHandler(policy, Status.REJECTED, ProcessingStatus.COMPLETED));

    }
}