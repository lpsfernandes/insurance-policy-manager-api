package io.insurance.policy.manager.application.scheduler;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RiskAnalysisTrigger extends ProcessingEventGenerator {

    private final String topicName;

    public RiskAnalysisTrigger(@Value("${spring.kafka.producer.topic.risk-analysis-processing}") String topicName,
                               EventSenderService eventSenderService,
                               ISearchPolicyService searchPolicyService) {
        super(eventSenderService, searchPolicyService);
        this.topicName = topicName;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 0)
    @SchedulerLock(name = "trigger.classify.risk.policy", lockAtMostFor = "PT5M")
    public void doWork() {
        this.generatesEvents(ProcessingStatus.AWAITING_RISK_ANALYSIS,
                ProcessingStatus.PROCESSING_RISK_ANALYSIS, this.topicName);
    }

}
