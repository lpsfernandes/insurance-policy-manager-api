package io.manager.policy.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.manager.policy.application.scheduler.dto.Event;
import io.manager.policy.application.scheduler.dto.Header;
import io.manager.policy.boundaries.driven.producer.KafkaProducer;
import io.manager.policy.domain.model.OutboxEvent;
import io.manager.policy.domain.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EventSubmissionService implements IEventSubmissionService{

    private final Duration expired;
    private final OutboxEventRepository outboxEventRepository;
    private final ISearchPolicyService searchPolicyService;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    public EventSubmissionService(@Value("${scheduler.send-event.maxTime:PT24H}") Duration expired,
                                  OutboxEventRepository outboxEventRepository,
                                  ISearchPolicyService searchPolicyService,
                                  KafkaProducer kafkaProducer,
                                  ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.searchPolicyService = searchPolicyService;
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
        this.expired = expired;
    }

    @Override
    public void sendEvent() {

        this.deleteExpiredEvents();

        var pageable = PageRequest.of(0, 1000, Sort.Direction.ASC, "createdAt");
        var pendingEvents = this.outboxEventRepository.findAll(pageable).getContent();
        log.debug("Bloco de evento para envio: {}", pendingEvents.size());
        for (var event : pendingEvents){
            this.sendEvent(event);
        }

    }

    void deleteExpiredEvents(){
        int expired = this.outboxEventRepository.deleteByCreatedAtBefore(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(this.expired.toHours()));
        log.debug("Eventos expirados: {}", expired);
    }

    void sendEvent(OutboxEvent event){
        log.debug("Iniciando thread assincrona envio do evento [{}]", event.getId());

        var mdcContext = MDC.getCopyOfContextMap();
        CompletableFuture.runAsync(() -> {
            try {

                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }

                log.debug("Preparando conteudo para envio");
                var header = this.objectMapper.writeValueAsString(Header.builder().traceId(MDC.get("traceId")).build());
                var message = this.objectMapper.writeValueAsString(this.searchPolicyService.getPolicyById(event.getPolicyId()).map(Event::new).orElseThrow());

                this.kafkaProducer.send(header, message);

                this.outboxEventRepository.delete(event);
            } catch (Exception e){
                log.error("Erro no processamento evento [{}]: {}", event.getId(), e.getMessage(), e);
            } finally {
                MDC.clear();
            }
        });
    }

}
