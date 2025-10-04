package io.manager.policy.application.scheduler;

import io.manager.policy.application.service.IEventSubmissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventTrigger {


    private final IEventSubmissionService eventSubmissionService;

    @Scheduled(fixedDelay = 1000, initialDelay = 0)
    @SchedulerLock(name = "trigger.send.event", lockAtMostFor = "PT5M")
    public void doWork() {
            this.eventSubmissionService.sendEvent();
    }

}
