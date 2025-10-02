package io.manager.policy.domain.service;

import io.manager.policy.api.dto.PolicyRequest;
import io.manager.policy.domain.model.Policy;

import java.util.List;
import java.util.Optional;

public interface IPolicyService {

    Optional<String> createPolicy(PolicyRequest request);
    Optional<Policy> getPolicy(String id);
    List<Policy> getPolicies();
}
