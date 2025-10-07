package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@AllArgsConstructor
public abstract class ProcessPolicy {

    private final IPolicyStatusHandler policyStatusHandler;
    private final BusinessMetricsCollector metricsCollector;

    void collectMetrics(Policy policy) {
        this.metricsCollector.recordPolicyProcessingTime(() -> Duration.between(policy.getCreatedAt(), policy.getFinishedAt()));
        if (policy.getStatus() == Status.APPROVED) {
            this.metricsCollector.incrementPoliciesApproved();
        } else {
            this.metricsCollector.incrementPoliciesRejected();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    void saveDataDb(Policy policy, Status status, ProcessingStatus processingStatus) {
        if ( status == Status.APPROVED || status == Status.REJECTED) {
            policy.setFinishedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            this.collectMetrics(policy);
        }

        if ( policy.getStatus() != status) {
            this.policyStatusHandler.processingStatusHandler(policy, status, processingStatus);
            return ;
        }

        this.policyStatusHandler.processingStatusHandler(policy, processingStatus);

    }

}
