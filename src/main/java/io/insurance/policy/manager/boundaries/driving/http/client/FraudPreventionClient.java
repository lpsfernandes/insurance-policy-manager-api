package io.insurance.policy.manager.boundaries.driving.http.client;

import io.insurance.policy.manager.boundaries.driving.http.dto.RiskAnalysisRequest;
import io.insurance.policy.manager.boundaries.driving.http.dto.RiskAnalysisResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fraud-prevention-engine")
public interface FraudPreventionClient {

    @PostMapping(value = "/analyze-risk", consumes = MediaType.APPLICATION_JSON_VALUE)
    RiskAnalysisResponse analyzeRisk(@RequestBody RiskAnalysisRequest request);

}
