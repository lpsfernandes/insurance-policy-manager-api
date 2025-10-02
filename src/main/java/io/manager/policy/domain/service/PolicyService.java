package io.manager.policy.domain.service;

import io.manager.policy.api.dto.PolicyRequest;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.Status;
import io.manager.policy.domain.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService implements IPolicyService{

    private final PolicyRepository policyRepository;

    @Override
    @Transactional
    public Optional<String> createPolicy(PolicyRequest request) {
        try {

            var policy = Policy.builder()
                    .status(Status.RECEIVED)
                    .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                    .build();

            return Optional.of(this.policyRepository.save(policy).getId());

        } catch (Exception e) {
            log.error("Falha ao tentar cadastrar apolice: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Policy> getPolicy(String id) {
        return this.policyRepository.findById(id);
    }

    @Override
    public List<Policy> getPolicies() {
        return List.of();
    }
}
