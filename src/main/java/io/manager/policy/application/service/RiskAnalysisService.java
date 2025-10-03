package io.manager.policy.application.service;

import io.manager.policy.boundaries.driving.http.client.FraudPreventionClient;
import io.manager.policy.boundaries.driving.http.dto.RiskAnalysisRequest;
import io.manager.policy.boundaries.driving.http.dto.RiskAnalysisResponse;
import io.manager.policy.domain.model.Occurrences;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.RiskAnalysis;
import io.manager.policy.domain.model.RiskClassification;
import io.manager.policy.domain.repository.OccurrencesRepository;
import io.manager.policy.domain.repository.RiskAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalysisService implements IRiskAnalysisService{

    private final FraudPreventionClient fraudPreventionClient;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final OccurrencesRepository occurrencesRepository;

    @Override
    public RiskClassification analyzeRisk(Policy policy) {
        log.debug("Enviado apolice {} para analise de risco", policy.getId());
        var response = this.fraudPreventionClient.analyzeRisk(RiskAnalysisRequest.builder()
                                                                    .clientId(policy.getClientId())
                                                                    .productId(policy.getProductId())
                                                                    .category(policy.getCategory())
                                                                    .insuredAmount(policy.getInsuredAmount())
                                                                    .monthlyPremium(policy.getMonthlyPremium())
                                                                    .coverages(policy.getCoverages())
                                                                    .assistances(policy.getAssistances())
                                                            .build());

        this.saveAnalyzeRisk(policy.getId(), response);

        return RiskClassification.valueOf(response.classification());
    }

    @Transactional(rollbackFor = Exception.class)
    void saveAnalyzeRisk(String id, RiskAnalysisResponse response){
        try {
            log.debug("Inserindo analise de risco {} no banco de dados", response.orderId());
            var riskAnalysis = this.riskAnalysisRepository.save(RiskAnalysis.builder()
                                                    .policyId(id)
                                                    .orderId(response.orderId())
                                                    .clientId(response.clientId())
                                                    .analyzedAt(response.analyzedAt())
                                                    .classification(response.classification())
                                              .build());

            for (var occurrence : response.occurrences()) {
                log.debug("Inserindo ocorrencia {} no banco de dados", occurrence.id());
                this.occurrencesRepository.save(Occurrences.builder()
                                .riskAnalysisId(riskAnalysis.getId())
                                .occurrenceId(occurrence.id())
                                .productId(occurrence.productId())
                                .type(occurrence.type())
                                .description(occurrence.description())
                                .createdAt(occurrence.createdAt())
                                .updatedAt(occurrence.updatedAt())
                            .build());
            }

        } catch (Exception e) {
            log.error("Falha ao tentar registrar analise de risco: {}", e.getMessage(), e);
        }
    }
}
