package io.manager.policy.application.service.interfaces;

import io.manager.policy.boundaries.driving.consumer.dto.InsuranceSubscriptionEvent;
import io.manager.policy.boundaries.driving.consumer.dto.PaymentEvent;

public interface IProcessApolicyService {
    void process(PaymentEvent event);
    void process(InsuranceSubscriptionEvent event);
}
