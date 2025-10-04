package io.manager.policy.application.service;

import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.enums.Status;

import java.util.Optional;

public interface IPolicyStatusHandler {
    Optional<Policy> statusHandler(String policyId, Status status);
}
