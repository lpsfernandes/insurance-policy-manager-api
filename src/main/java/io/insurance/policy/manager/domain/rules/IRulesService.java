package io.insurance.policy.manager.domain.rules;

public interface IRulesService {
    void applyRules(String traceId, String policyId);
}
