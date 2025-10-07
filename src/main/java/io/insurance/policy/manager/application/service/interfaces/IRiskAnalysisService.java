package io.insurance.policy.manager.application.service.interfaces;

public interface IRiskAnalysisService {
    void analyzeRisk(String traceId, String policyId);
}
