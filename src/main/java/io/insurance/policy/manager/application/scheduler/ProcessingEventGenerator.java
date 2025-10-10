package io.insurance.policy.manager.application.scheduler;

import io.insurance.policy.manager.application.service.ProcessingStatus;
import io.insurance.policy.manager.application.service.interfaces.ISearchPolicyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Slf4j
@AllArgsConstructor
public abstract class ProcessingEventGenerator {

    private final EventSenderService eventSenderService;
    private final ISearchPolicyService searchPolicyService;

    public void generatesEvents(ProcessingStatus searchStatus, ProcessingStatus statusForUpdate, String topic){
        var pageable = PageRequest.of(0, 1000, Sort.Direction.ASC, "createdAt");

        var policiesPending = this.searchPolicyService
                .getPolicyByProcessingStatus(searchStatus, pageable);

        policiesPending
                .forEach( p -> eventSenderService.sendEvent(p, statusForUpdate, topic));
    }

}
