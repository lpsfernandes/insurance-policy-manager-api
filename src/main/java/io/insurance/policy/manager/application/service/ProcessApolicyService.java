package io.insurance.policy.manager.application.service;

import io.insurance.policy.manager.application.service.interfaces.IProcessApolicyService;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.repository.PolicyRepository;
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
@Transactional(rollbackFor = Exception.class)
public class ProcessApolicyService extends HandleStatus implements IProcessApolicyService {

    private final PolicyRepository policyRepository;
    private final BusinessMetricsCollector metricsCollector;

    @Override
    public void process(PaymentEvent event) {
        this.policyRepository.findById(event.orderId())
                .ifPresentOrElse( p -> {
                            if (!event.paymentCompleted()) {
                                log.warn("Pagamento da apolice {} nao aprovado. Apolice sera rejeitada", event.orderId());
                                var oldStatus = p.getStatus();
                                p.setStatus(Status.REJECTED);
                                var reason = event.reason() != null ? event.reason() : "Pagamento nao realizado";
                                p.setReason(reason);
                                this.save(p, oldStatus);
                                this.metricsCollector.incrementkafkaEventsPaymentRejected();
                                return ;
                            }

                            log.debug("Pagamento da apolice {} realizado com sucesso", event.orderId());
                            var status = p.getSubscriptionDate() != null ? Status.APPROVED : Status.PENDING;
                            var oldStatus = p.getStatus();
                            p.setPaymentDate(event.paymentDateTime());
                            p.setStatus(status);
                            this.save(p, oldStatus);
                            this.metricsCollector.incrementkafkaEventsPaymentApproved();
                        },
                        () -> log.warn("Apolice {} nao encontrada. Evento de pagamento sera descartado", event.orderId()));

    }

    @Override
    public void process(InsuranceSubscriptionEvent event) {
        this.policyRepository.findById(event.orderId())
                .ifPresentOrElse( p -> {
                            if (!event.insuranceSubscriptionCompleted()) {
                                log.warn("Subscricao da apolice {} nao aprovado. Apolice sera rejeitada", event.orderId());
                                var oldStatus = p.getStatus();
                                p.setStatus(Status.REJECTED);
                                var reason = event.reason() != null ? event.reason() : "Subscricao nao aprovada";
                                p.setReason(reason);
                                this.save(p, oldStatus);
                                this.metricsCollector.incrementkafkaEventsSubscriptionRejected();
                                return ;
                            }

                            log.debug("Subscricao da apolice {} aprovada", event.orderId());
                            var status = p.getPaymentDate() != null ? Status.APPROVED : Status.PENDING;
                            var oldStatus = p.getStatus();
                            p.setStatus(status);
                            p.setSubscriptionDate(event.subscriptionDateTime());
                            this.save(p, oldStatus);
                            this.metricsCollector.incrementkafkaEventsSubscriptionApproved();
                        },
                        () -> log.warn("Apolice {} nao encontrada. Evento de subscricao sera descartado", event.orderId()));
    }


    void save(Policy policy, Status oldStatus) {
        if (this.lifeCycleCompleted(policy)) {
            policy.setFinishedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            this.collectMetrics(policy);
        }

        this.policyRepository.save(policy);
        if ( oldStatus != null && oldStatus != policy.getStatus()) {
            this.insertStatusHistory(policy.getId(), policy.getStatus());
            this.insertOutboxEvent(policy, ZonedDateTime.now(ZoneId.of("UTC")));
        }
    }

    boolean lifeCycleCompleted(Policy policy){
        return policy.getStatus() == Status.APPROVED || policy.getStatus() == Status.REJECTED;
    }

    void collectMetrics(Policy policy) {
        this.metricsCollector.recordPolicyProcessingTime(() -> Duration.between(policy.getCreatedAt(), policy.getFinishedAt()));
        if (policy.getStatus() == Status.APPROVED) {
            this.metricsCollector.incrementPoliciesApproved();
        } else {
            this.metricsCollector.incrementPoliciesRejected();
        }
    }
}
