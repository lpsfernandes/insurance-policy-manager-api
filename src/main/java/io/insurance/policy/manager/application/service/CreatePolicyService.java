package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.controller.dto.PolicyRequest;
import io.insurance.policy.manager.application.service.interfaces.ICreatePolicyService;
import io.insurance.policy.manager.domain.model.Assistances;
import io.insurance.policy.manager.domain.model.Coverage;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.StatusHistory;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.AssistancesRepository;
import io.insurance.policy.manager.domain.repository.CoverageRepository;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
import io.insurance.policy.manager.domain.repository.StatusHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.insurance.policy.manager.application.util.Helper.DEFAULT_DECIMAL_PLACES;

@Slf4j
@Service
@AllArgsConstructor
public class CreatePolicyService extends HandleStatus implements ICreatePolicyService {

    private final PolicyRepository policyRepository;
    private final CoverageRepository coverageRepository;
    private final AssistancesRepository assistancesRepositorylicy;
    private final StatusHistoryRepository statusHistoryRepository;
    private final BusinessMetricsCollector metricsCollector;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<Policy> createPolicy(PolicyRequest request) {
        try {
            log.debug("Processando requisicao para registro de apolice, cliente:{} /produto:{} /cetegoria:{}",
                    request.clientId(), request.productId(), request.category());

            String id = UUID.randomUUID().toString();

            var retPolicy = this.insertPolicy(id, request);

            this.insertAssistances(id, request.assistances());

            this.insertCoverages(id, request.coverages());

            this.insertStatusHistory(id, retPolicy.getStatus(), retPolicy.getCreatedAt());

            this.insertOutboxEvent(retPolicy);

            this.metricsCollector.incrementPoliciesCreated();

            return Optional.of(retPolicy);

        } catch (Exception e) {
            log.error("Falha ao tentar cadastrar apolice: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

     Policy insertPolicy(String id, PolicyRequest request) {
        log.debug("Inserindo apolice no banco de dados");
        var policy = Policy.builder()
                    .id(id)
                    .clientId(request.clientId())
                    .productId(request.productId())
                    .category(request.category().name())
                    .paymentMethod(request.paymentMethod().name())
                    .salesChannel(request.salesChannel().name())
                    .insuredAmount(request.insuredAmount().movePointRight(DEFAULT_DECIMAL_PLACES).longValue())
                    .monthlyPremium(request.monthlyPremium().movePointRight(DEFAULT_DECIMAL_PLACES).longValue())
                    .status(Status.RECEIVED)
                    .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        var retPolicy = this.policyRepository.save(policy);
        log.debug("Id da apolice: {}", id);
        return retPolicy;
    }

    void insertAssistances(String id, Set<String> assistances) {
        for (var assistance :  assistances) {
            log.debug("Inserindo assistencia - {} para apolice {}", assistance, id);
            this.assistancesRepositorylicy.save(Assistances.builder()
                    .policyId(id)
                    .assistance(assistance)
                    .build());
        }
    }

    void insertCoverages(String id, Map<String, BigDecimal> coverages) {
        for (Map.Entry<String, BigDecimal> value : coverages.entrySet()) {
            log.debug("Inserindo cobertura - {} para apolice {}", value.getKey(), id);
            this.coverageRepository.save(Coverage.builder()
                    .policyId(id)
                    .typeCoverage(value.getKey())
                    .insuredAmount(value.getValue().movePointRight(DEFAULT_DECIMAL_PLACES).longValue())
                    .build());
        }
    }

    void insertStatusHistory(String id, Status status, ZonedDateTime timestamp) {
        this.statusHistoryRepository.save(StatusHistory.builder()
                                                .policyId(id)
                                                .status(status)
                                                .createdAt(timestamp)
                                        .build());
    }


}
