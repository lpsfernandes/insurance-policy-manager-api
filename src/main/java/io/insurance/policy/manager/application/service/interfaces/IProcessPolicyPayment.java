package io.insurance.policy.manager.application.service.interfaces;

import io.insurance.policy.manager.boundaries.driving.consumer.dto.PaymentEvent;

public interface IProcessPolicyPayment {
    void process(PaymentEvent event);
}
