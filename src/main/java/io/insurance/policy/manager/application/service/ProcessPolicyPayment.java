package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.IProcessPolicyPayment;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProcessPolicyPayment extends ProcessPolicy implements IProcessPolicyPayment {

    private final ISearchPolicyService searchPolicyService;
    private final BusinessMetricsCollector metricsCollector;

    public ProcessPolicyPayment(ISearchPolicyService searchPolicyService,
                                IPolicyStatusHandler policyStatusHandler,
                                BusinessMetricsCollector metricsCollector) {
        super(policyStatusHandler, metricsCollector);
        this.searchPolicyService = searchPolicyService;
        this.metricsCollector = metricsCollector;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(PaymentEvent event) {
        this.searchPolicyService.getPolicyById(event.orderId())
                .ifPresentOrElse( p -> {
                            if (!event.paymentCompleted()) {
                                this.unauthorized(p, event);
                                return ;
                            }
                            this.authorized(p, event);
                        },
                        () -> log.warn("Apolice {} nao encontrada. Evento de pagamento sera descartado", event.orderId()));

    }

    void authorized(Policy policy, PaymentEvent event){
        log.debug("Pagamento da apolice {} realizado com sucesso", policy.getId());

        var status = policy.getSubscriptionDate() != null ? Status.APPROVED : Status.PENDING;

        var processingStatus = status == Status.APPROVED ? ProcessingStatus.COMPLETED :
                ProcessingStatus.AWAITING_SUBSCRIPTION;

        policy.setPaymentDate(event.paymentDateTime());

        this.saveDataDb(policy, status, processingStatus);

        this.metricsCollector.incrementkafkaEventsPaymentApproved();
    }

    void unauthorized(Policy policy, PaymentEvent event){
        log.warn("Pagamento da apolice {} nao aprovado. Apolice sera rejeitada", policy.getId());

        var reason = event.reason() != null ? event.reason() : "Pagamento nao realizado";

        policy.setReason(reason);

        this.saveDataDb(policy, Status.REJECTED, ProcessingStatus.COMPLETED);

        this.metricsCollector.incrementkafkaEventsPaymentRejected();
    }

}
