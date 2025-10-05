package io.manager.policy.application.service;

import io.manager.policy.application.service.interfaces.IEventSubmissionService;
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
public class EventSubmissionService implements IEventSubmissionService {

    private final Duration expired;
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducer kafkaProducer;
    private final BusinessMetricsCollector metricsCollector;

    public EventSubmissionService(@Value("${scheduler.send-event.maxTime:PT1M}") Duration expired,
                                  OutboxEventRepository outboxEventRepository,
                                  KafkaProducer kafkaProducer,
                                  BusinessMetricsCollector metricsCollector) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducer = kafkaProducer;
        this.expired = expired;
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void sendEvent() {
        var pageable = PageRequest.of(0, 1000, Sort.Direction.ASC, "createdAt");

        var eventsPendingProcessing = this.outboxEventRepository
                .findByEventsPendingProcessing(ZonedDateTime.now(ZoneId.of("UTC")),pageable);

        log.debug("Bloco de evento para envio: {}", eventsPendingProcessing.size());
        for (var event : eventsPendingProcessing){
            this.init(event);
            this.sendEvent(event);
        }

    }

    void init(OutboxEvent event){
        event.setMaxProcessingTime(ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(this.expired.toMinutes()));
        this.outboxEventRepository.save(event);
    }

    boolean checkEventExpired(OutboxEvent event){
        log.debug("Verificando se evento expirou");

        ZonedDateTime expirationTime = event.getCreatedAt().plusMinutes(this.expired.toMinutes());

        if ( ZonedDateTime.now().isAfter(expirationTime)) {
            log.debug("Evento expirado: {}", event.getId());
            this.outboxEventRepository.delete(event);
            return true;
        }
        return false;
    }

    void sendEvent(OutboxEvent event){
        log.debug("Iniciando thread assincrona envio do evento [{}]", event.getId());

        var mdcContext = MDC.getCopyOfContextMap();
        CompletableFuture.runAsync(() -> {
            try {

                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }

                if (this.checkEventExpired(event)) {
                    return ;
                }

                log.debug("Preparando conteudo para envio");

                var header = "{\"traceparent\":\"" + MDC.get("traceId") + "\"}";
                var message = event.getEventJson();

                this.kafkaProducer.send(header, message);

                this.metricsCollector.incrementKafkaEventProduced();

                this.outboxEventRepository.delete(event);
            } catch (Exception e){
                log.error("Erro no processamento evento [{}]: {}", event.getId(), e.getMessage(), e);
            } finally {
                MDC.clear();
            }
        });
    }

}
