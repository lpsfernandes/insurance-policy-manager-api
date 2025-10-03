package io.manager.policy.application.service;

import io.manager.policy.domain.model.OutboxEvent;
import io.manager.policy.domain.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
public abstract class HandleStatus {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Transactional
    public void insertOutboxEvent(String id, ZonedDateTime timestamp) {
        log.debug("Inserindo apolice {} para distribuicao via evento", id);
        this.outboxEventRepository.save(OutboxEvent.builder()
                .policyId(id)
                .createdAt(timestamp)
                .build());
    }

}
