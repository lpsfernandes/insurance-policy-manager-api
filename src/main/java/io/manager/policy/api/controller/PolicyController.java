package io.manager.policy.api.controller;

import io.manager.policy.api.dto.PolicyCreatedResponse;
import io.manager.policy.api.dto.PolicyRequest;
import io.manager.policy.api.dto.PolicyResponse;
import io.manager.policy.domain.exception.CreatePolicyException;
import io.manager.policy.domain.exception.PolicyNotFound;
import io.manager.policy.domain.service.IPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policy")
public class PolicyController {

    private final IPolicyService service;

    @PostMapping
    public PolicyCreatedResponse createPolicy(@RequestBody PolicyRequest request) {
        return this.service.createPolicy(request)
                    .map(PolicyCreatedResponse::new)
                    .orElseThrow(CreatePolicyException::new);
    }

    @GetMapping("/{id}")
    public PolicyResponse getPolicy(@RequestParam(name = "id") String policyId) {
        return this.service.getPolicy(policyId)
                .map(PolicyResponse::new)
                .orElseThrow(() -> new PolicyNotFound(policyId));
    }

    @GetMapping
    public List<PolicyResponse> getPolicies() {
        return this.service.getPolicies()
                .stream()
                .map(PolicyResponse::new)
                .toList();
    }
}