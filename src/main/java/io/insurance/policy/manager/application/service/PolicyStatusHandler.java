package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.domain.exception.StatusNotAllowed;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Optional<Policy> statusHandler(String policyId, Status status) {
        return this.policyRepository.findById(policyId)
                .flatMap( p -> {

                    if (p.getStatus() != null && p.getStatus() != status) {

                        if (!p.getStatus().canTransitionTo(status))
                            throw new StatusNotAllowed(p.getStatus(), status);

                        p.setStatus(status);

                        this.insertStatusHistory(p.getId(), p.getStatus());

                        this.insertOutboxEvent(p, ZonedDateTime.now(ZoneId.of("UTC")));

                        this.collectMetrics(p);

                        this.policyRepository.save(p);

                        return this.policyRepository.findById(policyId);

                    }

                    log.debug("Nao houve alteracao de status");

                    return Optional.of(p);

                });
    }

    @Override
    @Transactional
    public void processingStatusHandler(Policy p, ProcessingStatus processingStatus) {

        if (p.getProcessingStatus() != null
                && p.getProcessingStatus() != processingStatus) {

            if (!p.getProcessingStatus().canTransitionTo(processingStatus))
                throw new ProcessingStatusNotAllowed(p.getProcessingStatus(), processingStatus);

            p.setProcessingStatus(processingStatus);

            this.policyRepository.save(p);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processingStatusHandler(Policy p, Status status, ProcessingStatus processingStatus) {

        if (p.getProcessingStatus() != null
                && p.getProcessingStatus() != processingStatus) {

            if (!p.getProcessingStatus().canTransitionTo(processingStatus))
                throw new ProcessingStatusNotAllowed(p.getProcessingStatus(), processingStatus);

            p.setProcessingStatus(processingStatus);
        }

        if (p.getStatus() != null && p.getStatus() != status) {

            if (!p.getStatus().canTransitionTo(status))
                throw new StatusNotAllowed(p.getStatus(), status);

            p.setStatus(status);

            this.insertStatusHistory(p.getId(), p.getStatus());

            this.insertOutboxEvent(p, ZonedDateTime.now(ZoneId.of("UTC")));

            this.collectMetrics(p);
        }

        this.policyRepository.save(p);

    }

    void collectMetrics(Policy policy) {
        if (policy.getStatus() == Status.APPROVED) {
            this.metricsCollector.incrementPoliciesApproved();
        } else if (policy.getStatus() == Status.REJECTED) {
            this.metricsCollector.incrementPoliciesRejected();
        }
    }


}
