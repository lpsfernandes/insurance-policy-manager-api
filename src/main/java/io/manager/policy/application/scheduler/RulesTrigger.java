package io.manager.policy.application.scheduler;

import io.manager.policy.domain.rules.IRulesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class RulesTrigger {

    private final IRulesService rulesService;

    @Scheduled(fixedDelay = 1000, initialDelay = 0)
    @SchedulerLock(name = "trigger.rules.trigger", lockAtMostFor = "PT5M")
    public void doWork() {
        this.rulesService.analyze();
    }

}
