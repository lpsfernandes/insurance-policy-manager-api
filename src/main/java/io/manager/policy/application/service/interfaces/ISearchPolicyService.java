package io.manager.policy.application.service.interfaces;

import io.manager.policy.domain.model.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface ISearchPolicyService {
    Optional<Policy> getPolicyById(String id);
    Page<Policy> getPolicyByClientId(String id, PageRequest pageable);
    Page<Policy> getPolicies(PageRequest pageable);
}
