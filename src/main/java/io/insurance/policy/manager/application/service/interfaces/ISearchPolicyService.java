package io.insurance.policy.manager.application.service.interfaces;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.domain.model.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface ISearchPolicyService {
    Optional<Policy> getPolicyById(String id);
    Page<Policy> getPolicyByClientId(String id, PageRequest pageable);
    Page<Policy> getPolicies(PageRequest pageable);
    List<Policy> getPolicyByProcessingStatus(ProcessingStatus status, PageRequest pageable);
}
