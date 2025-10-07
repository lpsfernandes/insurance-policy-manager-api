package io.insurance.policy.manager.application.service.interfaces;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;

import java.util.Optional;

public interface IPolicyStatusHandler {
    Optional<Policy> statusHandler(String policyId, Status status);
    void processingStatusHandler(Policy policyId, ProcessingStatus processingStatus);
    void processingStatusHandler(Policy policyId, Status status, ProcessingStatus processingStatus);
}
