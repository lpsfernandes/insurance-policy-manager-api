package io.manager.policy.application.service;

import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.enums.RiskClassification;

public interface IRiskAnalysisService {
    RiskClassification analyzeRisk(Policy policy);
}
