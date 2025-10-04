package io.manager.policy.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.manager.policy.domain.model.OutboxEvent;
import io.manager.policy.domain.model.Policy;
import io.manager.policy.domain.model.events.EventBody;
import io.manager.policy.domain.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Slf4j
public abstract class HandleStatus {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

}
