package io.insurance.policy.manager.application.service.interfaces;

import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;

public interface IRiskAnalysisService {
    RiskClassification analyzeRisk(Policy policy);
}
