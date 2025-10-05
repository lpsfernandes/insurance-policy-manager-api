package io.manager.policy.application.service;

import io.manager.policy.application.service.interfaces.IPolicyStatusHandler;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.enums.Status;
import io.manager.policy.domain.repository.PolicyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
                    this.collectMetrics(p);
                    return Optional.of(p);
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
