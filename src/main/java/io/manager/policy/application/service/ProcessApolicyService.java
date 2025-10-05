package io.manager.policy.application.service;

import io.manager.policy.application.service.interfaces.IProcessApolicyService;
import io.manager.policy.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.manager.policy.boundaries.driving.consumer.dto.PaymentEvent;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.enums.Status;
import io.manager.policy.domain.repository.PolicyRepository;
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
                            if (event.paymentCompleted()) {
                                log.warn("Pagamento da apolice {} nao aprovado. Apolice sera rejeitada", event.orderId());
                                p.setStatus(Status.REJECTED);
                                var reason = p.getReason() != null ? p.getReason() : "Pagamento nao realizado";
                                p.setReason(reason);
                                this.save(p);
                                this.metricsCollector.incrementkafkaEventsPaymentRejected();
                                return ;
                            }

                            log.debug("Pagamento da apolice {} realizado com sucesso", event.orderId());
                            var status = p.getSubscriptionDate() != null ? Status.APPROVED : Status.PENDING;
                            p.setPaymentDate(event.paymentDateTime());
                            p.setStatus(status);
                            this.save(p);
                            this.metricsCollector.incrementkafkaEventsPaymentApproved();
                        },
                        () -> log.warn("Apolice {} nao encontrada. Evento de pagamento sera descartado", event.orderId()));

    }

    @Override
    public void process(InsuranceSubscriptionEvent event) {
        this.policyRepository.findById(event.orderId())
                .ifPresentOrElse( p -> {
                            if (event.insuranceSubscriptionCompleted()) {
                                log.warn("Subscricao da apolice {} nao aprovado. Apolice sera rejeitada", event.orderId());
                                p.setStatus(Status.REJECTED);
                                var reason = p.getReason() != null ? p.getReason() : "Subscricao nao aprovada";
                                p.setReason(reason);
                                this.save(p);
                                this.metricsCollector.incrementkafkaEventsSubscriptionRejected();
                                return ;
                            }

                            log.debug("Subscricao da apolice {} aprovada", event.orderId());
                            var status = p.getPaymentDate() != null ? Status.APPROVED : Status.PENDING;
                            p.setStatus(status);
                            p.setSubscriptionDate(event.subscriptionDateTime());
                            this.save(p);
                            this.metricsCollector.incrementkafkaEventsSubscriptionApproved();
                        },
                        () -> log.warn("Apolice {} nao encontrada. Evento de subscricao sera descartado", event.orderId()));
    }


    void save(Policy policy) {
        if (this.lifeCycleCompleted(policy)) {
            policy.setFinishedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            this.collectMetrics(policy);
        }

        this.insertOutboxEvent(policy, ZonedDateTime.now(ZoneId.of("UTC")));

        this.policyRepository.save(policy);
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
