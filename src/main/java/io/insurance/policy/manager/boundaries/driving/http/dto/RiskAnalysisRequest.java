package io.insurance.policy.manager.boundaries.driving.http.dto;

import io.insurance.policy.manager.domain.model.Assistances;
import io.insurance.policy.manager.domain.model.Coverage;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RiskAnalysisRequest {

    private String clientId;

    private Long productId;

    private String category;

    private Long insuredAmount;

    private Long monthlyPremium;

    private Set<Coverage> coverages;

    private Set<Assistances> assistances;

}
