package io.insurance.policy.manager.application.service.interfaces;

import io.insurance.policy.manager.application.controller.dto.PolicyRequest;
import io.insurance.policy.manager.domain.model.Policy;

import java.util.Optional;

public interface ICreatePolicyService {
    Optional<Policy> createPolicy(PolicyRequest request);
}
