package io.manager.policy.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetricsCollector {

    private final MeterRegistry meterRegistry;

    public BusinessMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private Counter policiesCreated;
    private Counter policiesApproved;
    private Counter policiesRejected;
    private Counter kafkaEventsProduced;
    private Counter kafkaEventsPaymentConsumed;
    private Counter kafkaEventsPaymentRejected;
    private Counter kafkaEventsPaymentApproved;
    private Counter kafkaEventsSubscriptionConsumed;
    private Counter kafkaEventsSubscriptionRejected;
    private Counter kafkaEventsSubscriptionApproved;
    private Timer policyProcessingTimer;

    @PostConstruct
    public void initMetrics() {
        policiesCreated = meterRegistry.counter("business.policies.created");
        policiesApproved = meterRegistry.counter("business.policies.approved");
        policiesRejected = meterRegistry.counter("business.policies.rejected");
        kafkaEventsProduced = meterRegistry.counter("business.kafka.events.produced");
        kafkaEventsPaymentConsumed = meterRegistry.counter("business.kafka.events.payment.consumed");
        kafkaEventsPaymentApproved = meterRegistry.counter("business.kafka.events.payment.approved");
        kafkaEventsPaymentRejected = meterRegistry.counter("business.kafka.events.payment.rejected");
        kafkaEventsSubscriptionConsumed = meterRegistry.counter("business.kafka.subscription.consumed");
        kafkaEventsSubscriptionApproved = meterRegistry.counter("business.kafka.subscription.approved");
        kafkaEventsSubscriptionRejected = meterRegistry.counter("business.kafka.subscription.rejected");
        policyProcessingTimer = meterRegistry.timer("business.policy.processing.duration");
    }

    public void incrementPoliciesCreated() {
        policiesCreated.increment();
    }

    public void incrementPoliciesApproved() {
        policiesApproved.increment();
    }

    public void incrementPoliciesRejected() {
        policiesRejected.increment();
    }

    public void incrementKafkaEventProduced() {
        kafkaEventsProduced.increment();
    }

    public void incrementKafkaPaymentEventConsumed() {
        kafkaEventsPaymentConsumed.increment();
    }

    public void incrementkafkaEventsPaymentApproved() {
        kafkaEventsPaymentApproved.increment();
    }

    public void incrementkafkaEventsPaymentRejected() {
        kafkaEventsPaymentRejected.increment();
    }

    public void incrementKafkaSubscriptionEventConsumed() {
        kafkaEventsSubscriptionConsumed.increment();
    }

    public void incrementkafkaEventsSubscriptionApproved() {
        kafkaEventsSubscriptionApproved.increment();
    }

    public void incrementkafkaEventsSubscriptionRejected() {
        kafkaEventsSubscriptionRejected.increment();
    }

    public void incrementRiskAnalysis(String classification) {
        this.meterRegistry.counter("business.policy.risk.analysis", "status", classification).increment();
    }

    public void recordPolicyProcessingTime(Runnable processingLogic) {
        policyProcessingTimer.record(processingLogic);
    }
}