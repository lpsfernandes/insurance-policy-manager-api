package io.insurance.policy.manager.application.scheduler;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.application.service.interfaces.IPolicyStatusHandler;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import io.insurance.policy.manager.boundaries.driven.producer.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RulesTrigger extends ProcessingEventGenerator {

    private final String topicName;

    public RulesTrigger(@Value("${spring.kafka.producer.topic.rule-processing}") String topicName,
                        EventSenderService eventSenderService,
                        ISearchPolicyService searchPolicyService) {
        super(eventSenderService, searchPolicyService);
        this.topicName = topicName;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 0)
    @SchedulerLock(name = "trigger.rules.trigger", lockAtMostFor = "PT5M")
    public void doWork() {
        this.generatesEvents(ProcessingStatus.AWAITING_RULES_VALIDATION,
                ProcessingStatus.VALIDATING_RULES, this.topicName);
    }

}
