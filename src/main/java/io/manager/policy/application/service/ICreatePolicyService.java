package io.manager.policy.application.service;

import io.manager.policy.application.controller.dto.PolicyRequest;
import io.manager.policy.domain.model.Policy;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface ICreatePolicyService {
    Optional<Policy> createPolicy(PolicyRequest request);
}
