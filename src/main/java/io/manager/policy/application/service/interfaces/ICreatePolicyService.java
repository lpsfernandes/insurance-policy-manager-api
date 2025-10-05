package io.manager.policy.application.service.interfaces;

import io.manager.policy.application.controller.dto.PolicyRequest;
import io.manager.policy.domain.model.Policy;

import java.util.Optional;

public interface ICreatePolicyService {
    Optional<Policy> createPolicy(PolicyRequest request);
}
