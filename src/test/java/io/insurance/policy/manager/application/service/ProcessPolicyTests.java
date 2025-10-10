package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ProcessPolicyTests {

    @Mock
    private IPolicyStatusHandler policyStatusHandler;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private TestableProcessPolicy processPolicy;

    private Policy policy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processPolicy = new TestableProcessPolicy(policyStatusHandler, metricsCollector);

        policy = new Policy();
        policy.setId("POL123");
        policy.setCreatedAt(ZonedDateTime.now().minusHours(2));
        policy.setStatus(Status.APPROVED);
    }

    @Test
    void shouldCollectApprovedMetrics() {
        policy.setFinishedAt(ZonedDateTime.now());

        processPolicy.collectMetrics(policy.getStatus(), policy.getCreatedAt(), policy.getFinishedAt());

        verify(metricsCollector).recordPolicyProcessingTime(any());
        verify(metricsCollector).incrementPoliciesApproved();
        verify(metricsCollector, never()).incrementPoliciesRejected();
    }

    @Test
    void shouldCollectRejectedMetrics() {
        policy.setStatus(Status.REJECTED);
        policy.setFinishedAt(ZonedDateTime.now());

        processPolicy.collectMetrics(Status.REJECTED, policy.getCreatedAt(), policy.getFinishedAt());

        verify(metricsCollector).recordPolicyProcessingTime(any());
        verify(metricsCollector).incrementPoliciesRejected();
        verify(metricsCollector, never()).incrementPoliciesApproved();
    }

    @Test
    void shouldSaveApprovedStatusAndCollectMetrics() {
        policy.setStatus(Status.PENDING);

        processPolicy.saveDataDb(policy, Status.APPROVED, ProcessingStatus.COMPLETED);

        assertNotNull(policy.getFinishedAt());
        verify(metricsCollector).recordPolicyProcessingTime(any());
        verify(metricsCollector).incrementPoliciesApproved();
        verify(policyStatusHandler).processingStatusHandler(policy, Status.APPROVED, ProcessingStatus.COMPLETED);
    }

    @Test
    void shouldSaveSameStatusWithoutMetrics() {
        policy.setStatus(Status.APPROVED);
        policy.setFinishedAt(ZonedDateTime.now());

        processPolicy.saveDataDb(policy, Status.APPROVED, ProcessingStatus.COMPLETED);

        verify(policyStatusHandler).processingStatusHandler(policy, ProcessingStatus.COMPLETED);
        verify(metricsCollector).recordPolicyProcessingTime(any());
        verify(metricsCollector).incrementPoliciesApproved();
    }

    static class TestableProcessPolicy extends ProcessPolicy {
        public TestableProcessPolicy(IPolicyStatusHandler handler, BusinessMetricsCollector collector) {
            super(handler, collector);
        }
    }

}