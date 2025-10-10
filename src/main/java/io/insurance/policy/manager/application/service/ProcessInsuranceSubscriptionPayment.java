package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.IProcessInsuranceSubscriptionPayment;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProcessInsuranceSubscriptionPayment extends ProcessPolicy implements IProcessInsuranceSubscriptionPayment {

    private final ISearchPolicyService searchPolicyService;
    private final BusinessMetricsCollector metricsCollector;

    public ProcessInsuranceSubscriptionPayment(ISearchPolicyService searchPolicyService,
                                IPolicyStatusHandler policyStatusHandler,
                                BusinessMetricsCollector metricsCollector) {
        super(policyStatusHandler, metricsCollector);
        this.searchPolicyService = searchPolicyService;
        this.metricsCollector = metricsCollector;
    }

    @Override
    @Transactional
    public void process(InsuranceSubscriptionEvent event) {
        this.searchPolicyService.getPolicyById(event.orderId())
                .ifPresentOrElse( p -> {
                            if (!event.insuranceSubscriptionCompleted()) {
                                this.unauthorized(p, event);
                                return ;
                            }
                            this.authorized(p, event);
                        },
                        () -> log.warn("Apolice {} nao encontrada. Evento de subscricao sera descartado", event.orderId()));
    }

    void authorized(Policy policy, InsuranceSubscriptionEvent event){
        log.debug("Subscricao da apolice {} aprovada", event.orderId());

        var status = policy.getPaymentDate() != null ? Status.APPROVED : Status.PENDING;

        var processingStatus = status == Status.APPROVED ? ProcessingStatus.COMPLETED :
                ProcessingStatus.AWAITING_PAYMENT;

        policy.setSubscriptionDate(event.subscriptionDateTime());

        this.saveDataDb(policy, status, processingStatus);

        this.metricsCollector.incrementkafkaEventsSubscriptionApproved();
    }

    void unauthorized(Policy policy, InsuranceSubscriptionEvent event){
        log.warn("Subscricao da apolice {} nao aprovado. Apolice sera rejeitada", event.orderId());

        var reason = event.reason() != null ? event.reason() : "Subscricao nao aprovada";

        policy.setReason(reason);

        this.saveDataDb(policy, Status.REJECTED, ProcessingStatus.COMPLETED);

        this.metricsCollector.incrementkafkaEventsSubscriptionRejected();
    }

}
