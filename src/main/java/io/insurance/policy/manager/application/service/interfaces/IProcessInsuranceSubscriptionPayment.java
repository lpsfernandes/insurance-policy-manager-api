package io.insurance.policy.manager.application.service.interfaces;

import io.insurance.policy.manager.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;

public interface IProcessInsuranceSubscriptionPayment {
    void process(InsuranceSubscriptionEvent event);
}
