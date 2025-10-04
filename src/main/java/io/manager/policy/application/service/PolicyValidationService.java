package io.manager.policy.application.service;

import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.enums.Status;
import io.manager.policy.domain.repository.PolicyRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PolicyValidationService extends HandleStatus implements IPolicyValidationService{

    private final Duration expired;
    private final PolicyRepository policyRepository;
    private final IRiskAnalysisService riskAnalysisService;


    protected PolicyValidationService(@Value("${scheduler.risk-analysis.maxTime:PT1M}") Duration expired,
                                      PolicyRepository policyRepository,
                                      IRiskAnalysisService riskAnalysisService) {
        this.policyRepository = policyRepository;
        this.riskAnalysisService = riskAnalysisService;
        this.expired = expired;
    }

    @Override
    public void classifyRisk() {

        var pageable = PageRequest.of(0, 1000, Sort.Direction.ASC, "createdAt");
        var policiesRiskAnalysis = this.policyRepository
                .findByPoliciesPendingProcessing(Status.RECEIVED,
                        ZonedDateTime.now(ZoneId.of("UTC")),
                        pageable);

        log.debug("Bloco de apolices para analise de risco: {}", policiesRiskAnalysis.size());
        for (var policy : policiesRiskAnalysis){
            this.init(policy);
            this.classifyRiskPolicy(policy);
        }

    }

    void init(Policy policy){
        policy.setMaxProcessingTime(ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(this.expired.toMinutes()));
        this.policyRepository.save(policy);
    }

    void classifyRiskPolicy(Policy policy) {
        log.debug("Iniciando thread assincrona para analise de risco da apolice {}", policy.getId());

        var mdcContext = MDC.getCopyOfContextMap();
        CompletableFuture.runAsync(() -> {
            try {
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                var risk = this.riskAnalysisService.analyzeRisk(policy);
                log.debug("Apolice {} com classificacao {}", policy.getId(), risk);

                policy.setRiskClassification(risk);
                policy.setStatus(Status.VALIDATED);
                policy.setMaxProcessingTime(null);

                this.updateDatabaseAndInsertOutboxEvent(policy);
            } catch (Exception e){
                log.error("Erro durante a analise de risco da apolice {}: {}", policy.getId(), e.getMessage(), e);
            } finally {
                MDC.clear();
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    void updateDatabaseAndInsertOutboxEvent(Policy policy) {
        this.policyRepository.save(policy);
        this.insertOutboxEvent(policy, ZonedDateTime.now(ZoneId.of("UTC")));
    }

}
