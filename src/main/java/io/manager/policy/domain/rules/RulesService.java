package io.manager.policy.domain.rules;

import io.manager.policy.application.service.BusinessMetricsCollector;
import io.manager.policy.application.service.HandleStatus;
import io.manager.policy.domain.exception.RiskClassificationException;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.enums.Status;
import io.manager.policy.domain.repository.PolicyRepository;
import io.manager.policy.domain.repository.RulesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class RulesService extends HandleStatus implements IRulesService {

    private final Duration expired;
    private final PolicyRepository policyRepository;
    private final RulesRepository rulesRepository;
    private final BusinessMetricsCollector metricsCollector;

    public RulesService(@Value("${scheduler.rules-analysis.maxTime:PT1M}") Duration expired,
                        PolicyRepository policyRepository,
                        RulesRepository rulesRepository, BusinessMetricsCollector metricsCollector) {
        this.expired = expired;
        this.policyRepository = policyRepository;
        this.rulesRepository = rulesRepository;
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void analyze() {

        var pageable = PageRequest.of(0, 1000, Sort.Direction.ASC, "createdAt");
        var policiesRulesAnalysis = this.policyRepository
                .findByPoliciesPendingProcessing(Status.VALIDATED,
                        ZonedDateTime.now(ZoneId.of("UTC")),
                        pageable);

        log.debug("Bloco de apolices para validacao das regras: {}", policiesRulesAnalysis.size());
        for (var policy : policiesRulesAnalysis){
            this.init(policy);
            this.applyRules(policy);
        }
    }

    void init(Policy policy){
        policy.setMaxProcessingTime(ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(this.expired.toMinutes()));
        this.policyRepository.save(policy);
    }

    void applyRules(Policy policy) {
        if (policy.getRiskClassification() == null)
            throw new RiskClassificationException();

        log.debug("Analisando regras para apolice {} classificacao {}", policy.getId(), policy.getRiskClassification());

        var rules = this.rulesRepository.findByRiskClassification(policy.getRiskClassification())
                .stream()
                .filter(r -> r.getCategory() == null || r.getCategory().equals(policy.getCategory()))
                .filter(r -> policy.getInsuredAmount() <= r.getInsuredAmountLimit())
                .toList();

        rules.forEach(r -> log.debug("Apolice validada pela regra {}", r));

        var status = rules.isEmpty() ? Status.REJECTED : Status.PENDING;
        var finishedAt = status == Status.REJECTED ? ZonedDateTime.now(ZoneId.of("UTC")) : null;

        policy.setFinishedAt(finishedAt);
        policy.setStatus(status);
        policy.setMaxProcessingTime(null);

        this.collectMetrics(policy);

        this.updateDatabaseAndInsertOutboxEvent(policy);

    }

    void collectMetrics(Policy policy) {
        if (policy.getStatus() == Status.REJECTED) {
            this.metricsCollector.incrementPoliciesRejected();
            this.metricsCollector.recordPolicyProcessingTime(() ->
                    Duration.between(policy.getCreatedAt(), policy.getFinishedAt()));
        }
    }

    @Transactional
    void updateDatabaseAndInsertOutboxEvent(Policy policy) {
        this.policyRepository.save(policy);
        this.insertOutboxEvent(policy, ZonedDateTime.now(ZoneId.of("UTC")));
    }
}
