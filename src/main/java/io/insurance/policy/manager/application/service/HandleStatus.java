package io.insurance.policy.manager.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.insurance.policy.manager.domain.model.OutboxEvent;
import io.insurance.policy.manager.domain.model.Policy;
import io.insurance.policy.manager.domain.model.StatusHistory;
import io.insurance.policy.manager.domain.model.enums.Status;
import io.insurance.policy.manager.domain.model.events.EventBody;
import io.insurance.policy.manager.domain.repository.OutboxEventRepository;
import io.insurance.policy.manager.domain.repository.StatusHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@Slf4j
public abstract class HandleStatus {

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    StatusHistoryRepository statusHistoryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Transactional
    public void insertOutboxEvent(Policy policy) {

        try {
            log.debug("Inserindo apolice {} para distribuicao via evento", policy.getStatus());

            var event = new EventBody(policy);

            this.outboxEventRepository.save(OutboxEvent.builder()
                    .eventJson(this.objectMapper.writeValueAsString(event))
                    .createdAt(policy.getCreatedAt())
                    .build());

        } catch (JsonProcessingException e){
            log.error("Nao foi possivel gerar registro para envio de evento = {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void insertOutboxEvent(Policy policy, ZonedDateTime eventDateTime) {

        try {
            log.debug("Inserindo apolice {} para distribuicao via evento", policy.getStatus());

            var event = new EventBody(policy);

            this.outboxEventRepository.save(OutboxEvent.builder()
                    .eventJson(this.objectMapper.writeValueAsString(event))
                    .createdAt(eventDateTime)
                    .build());

        } catch (JsonProcessingException e){
            log.error("Nao foi possivel gerar registro para envio de evento = {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void insertStatusHistory(String policyId, Status status){
        this.statusHistoryRepository.save(StatusHistory.builder()
                        .policyId(policyId)
                        .status(status)
                        .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build());
    }

}
