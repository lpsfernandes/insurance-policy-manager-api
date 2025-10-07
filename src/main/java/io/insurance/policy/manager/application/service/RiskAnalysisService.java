package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.IRiskAnalysisService;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driven.http.client.FraudPreventionClient;
import io.insurance.policy.manager.boundaries.driven.http.dto.RiskAnalysisRequest;
import io.insurance.policy.manager.boundaries.driven.http.dto.RiskAnalysisResponse;
import io.insurance.policy.manager.domain.exception.PolicyNotFound;
import io.insurance.policy.manager.domain.model.Occurrences;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.RiskAnalysis;
import io.insurance.policy.manager.domain.model.enums.RiskClassification;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.OccurrencesRepository;
import io.insurance.policy.manager.domain.repository.RiskAnalysisRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAnalysisService implements IRiskAnalysisService {

    private final ISearchPolicyService searchPolicyService;
    private final IPolicyStatusHandler policyStatusHandler;
    private final FraudPreventionClient fraudPreventionClient;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final OccurrencesRepository occurrencesRepository;
    private final BusinessMetricsCollector metricsCollector;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void analyzeRisk(String traceId, @NonNull String policyId) {
        log.debug("Enviado apolice {} para analise de risco, requisicao: {}", policyId, traceId);

        var policy = this.searchPolicyService.getPolicyById(policyId)
                                .orElseThrow(() -> new PolicyNotFound(policyId));

        var response = this.fraudPreventionClient.analyzeRisk(RiskAnalysisRequest.builder()
                                                                    .clientId(policy.getClientId())
                                                                    .productId(policy.getProductId())
                                                                    .category(policy.getCategory())
                                                                    .insuredAmount(policy.getInsuredAmount())
                                                                    .monthlyPremium(policy.getMonthlyPremium())
                                                                    .coverages(policy.getCoverages())
                                                                    .assistances(policy.getAssistances())
                                                            .build());

        policy.setRiskClassification(RiskClassification.valueOf(response.classification()));

        this.saveDataDb(policy, response);
        this.metricsCollector.incrementRiskAnalysis(response.classification().toLowerCase());

    }


    void saveDataDb(Policy policy, RiskAnalysisResponse response) {

        this.policyStatusHandler.processingStatusHandler(policy,
                Status.VALIDATED, ProcessingStatus.AWAITING_RULES_VALIDATION);

        this.saveAnalyzeRisk(policy.getId(), response);
    }


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
