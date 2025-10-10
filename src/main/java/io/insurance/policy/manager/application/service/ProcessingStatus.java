package io.insurance.policy.manager.application.service;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum ProcessingStatus {
    AWAITING_RISK_ANALYSIS,
    PROCESSING_RISK_ANALYSIS,
    AWAITING_RULES_VALIDATION,
    VALIDATING_RULES,
    AWAITING_PAYMENT_AND_SUBSCRIPTION,
    AWAITING_PAYMENT,
    AWAITING_SUBSCRIPTION,
    COMPLETED;


    private Set<ProcessingStatus> allowedTransitions;

    static {
        AWAITING_RISK_ANALYSIS.allowedTransitions = EnumSet.of(PROCESSING_RISK_ANALYSIS);
        PROCESSING_RISK_ANALYSIS.allowedTransitions = EnumSet.of(AWAITING_RULES_VALIDATION);
        AWAITING_RULES_VALIDATION.allowedTransitions = EnumSet.of(VALIDATING_RULES, COMPLETED);
        VALIDATING_RULES.allowedTransitions = EnumSet.of(AWAITING_PAYMENT_AND_SUBSCRIPTION, COMPLETED);
        AWAITING_PAYMENT_AND_SUBSCRIPTION.allowedTransitions = EnumSet.of(AWAITING_PAYMENT, AWAITING_SUBSCRIPTION, COMPLETED);
        AWAITING_PAYMENT.allowedTransitions = EnumSet.of(AWAITING_SUBSCRIPTION, COMPLETED);
        AWAITING_SUBSCRIPTION.allowedTransitions = EnumSet.of(AWAITING_PAYMENT, COMPLETED);
        COMPLETED.allowedTransitions = EnumSet.noneOf(ProcessingStatus.class);
    }

    public boolean canTransitionTo(ProcessingStatus target) {
        return allowedTransitions.contains(target);
    }

}
