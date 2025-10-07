package io.insurance.policy.manager.domain.rules;

import io.insurance.policy.manager.application.service.BusinessMetricsCollector;
import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.domain.exception.PolicyNotFound;
import io.insurance.policy.manager.domain.exception.RiskClassificationException;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.RulesRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class RulesService implements IRulesService {

    private final ISearchPolicyService searchPolicyService;
    private final IPolicyStatusHandler policyStatusHandler;
    private final RulesRepository rulesRepository;
    private final BusinessMetricsCollector metricsCollector;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRules(String traceId, String policyId) {

        var policy = this.searchPolicyService.getPolicyById(policyId)
                .orElseThrow(() -> new PolicyNotFound(policyId));

        if (policy.getRiskClassification() == null)
            throw new RiskClassificationException();

        log.debug("Analisando regras para apolice {} classificacao {}, requisicao: {}",
                policy.getId(), policy.getRiskClassification(), traceId);

        var rules = this.rulesRepository.findByRiskClassification(policy.getRiskClassification())
                .stream()
                .filter(r -> r.getCategory() == null || r.getCategory().equals(policy.getCategory()))
                .filter(r -> policy.getInsuredAmount() <= r.getInsuredAmountLimit())
                .toList();

        rules.forEach(r -> log.debug("Apolice validada pela regra {}", r));

        this.collectMetrics(policy);

        this.saveDataDb(policy, !rules.isEmpty());

    }

    void saveDataDb(Policy policy, boolean meetsRules) {

        var status = meetsRules ? Status.PENDING : Status.REJECTED;

        if (status == Status.REJECTED) {
            policy.setFinishedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        }

        var processingStatus = status == Status.REJECTED ? ProcessingStatus.COMPLETED
                : ProcessingStatus.AWAITING_PAYMENT_AND_SUBSCRIPTION;

        this.policyStatusHandler.processingStatusHandler(policy, status, processingStatus);

    }

    public void collectMetrics(Policy policy) {
        if (policy.getStatus() == Status.REJECTED) {
            this.metricsCollector.incrementPoliciesRejected();
            this.metricsCollector.recordPolicyProcessingTime(() ->
                    Duration.between(policy.getCreatedAt(), policy.getFinishedAt()));
        }
    }
}
