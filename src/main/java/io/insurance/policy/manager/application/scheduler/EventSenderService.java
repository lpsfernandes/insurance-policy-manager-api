package io.insurance.policy.manager.application.scheduler;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driven.producer.KafkaProducer;
import io.insurance.policy.manager.domain.model.Policy;
import lombok.AllArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class EventSenderService {

    private final IPolicyStatusHandler policyStatusHandler;
    private final KafkaProducer kafkaProducer;

    @Transactional(rollbackFor = Exception.class)
    public void sendEvent(Policy p, ProcessingStatus statusForUpdate, String topic) {

        var header = "{\"traceparent\":\"" + MDC.get("traceId") + "\"}";
        var message = "{\"policyId\":\"" + p.getId() + "\"}";

        policyStatusHandler.processingStatusHandler(p, statusForUpdate);
        kafkaProducer.send(header, message, topic);
    }
}