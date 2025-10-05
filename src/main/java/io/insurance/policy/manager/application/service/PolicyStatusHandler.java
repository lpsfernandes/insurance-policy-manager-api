package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PolicyStatusHandler extends HandleStatus implements IPolicyStatusHandler {

    private final PolicyRepository policyRepository;
    private final BusinessMetricsCollector metricsCollector;

    @Override
    public Optional<Policy> statusHandler(String policyId, Status status) {
        return this.policyRepository.findById(policyId)
                .flatMap( p -> {
                    p.setStatus(status);
                    this.policyRepository.save(p);
                    this.insertStatusHistory(p.getId(), p.getStatus());
                    this.insertOutboxEvent(p, ZonedDateTime.now(ZoneId.of("UTC")));
                    this.collectMetrics(p);
                    return this.policyRepository.findById(policyId);
                });
    }

    void collectMetrics(Policy policy) {
        if (policy.getStatus() == Status.APPROVED) {
            this.metricsCollector.incrementPoliciesApproved();
        } else if (policy.getStatus() == Status.REJECTED) {
            this.metricsCollector.incrementPoliciesRejected();
        }
    }


}
