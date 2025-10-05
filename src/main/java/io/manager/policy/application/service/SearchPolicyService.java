package io.manager.policy.application.service;

import io.manager.policy.application.service.interfaces.ISearchPolicyService;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchPolicyService implements ISearchPolicyService {

    private final PolicyRepository policyRepository;

    @Override
    public Optional<Policy> getPolicyById(String id) {
        return this.policyRepository.findById(id);
    }

    @Override
    public Page<Policy> getPolicyByClientId(String id, PageRequest pageable) {
        return this.policyRepository.findByClientId(id, pageable);
    }

    @Override
    public Page<Policy> getPolicies(PageRequest pageable) {
        return this.policyRepository.findAll(pageable);
    }

}
