package io.insurance.policy.manager.application.service;


import io.insurance.policy.manager.boundaries.driven.producer.KafkaProducer;
import io.insurance.policy.manager.domain.model.OutboxEvent;
import io.insurance.policy.manager.domain.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSubmissionServiceTests {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private BusinessMetricsCollector metricsCollector;

    private EventSubmissionService service;

    private OutboxEvent event;

    @BeforeEach
    void setUp() {
        Duration expired = Duration.ofMinutes(1);
        service = new EventSubmissionService(expired, "", outboxEventRepository, kafkaProducer, metricsCollector);

        event = new OutboxEvent();
        event.setId(123L);
        event.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(2));
        event.setEventJson("{\"data\":\"test\"}");
    }

    @Test
    void testInitShouldSetMaxProcessingTimeAndSaveEvent() {
        service.init(event);

        assertNotNull(event.getMaxProcessingTime());
        verify(outboxEventRepository).save(event);
    }

    @Test
    void testCheckEventExpiredShouldDeleteExpiredEvent() {
        boolean expired = service.checkEventExpired(event);

        assertTrue(expired);
        verify(outboxEventRepository).delete(event);
    }

    @Test
    void testCheckEventExpiredShouldNotDeleteValidEvent() {
        event.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusSeconds(10));

        boolean expired = service.checkEventExpired(event);

        assertFalse(expired);
        verify(outboxEventRepository, never()).delete(event);
    }

    @Test
    void testSendEventShouldProcessAndDeleteEvent() throws InterruptedException {
        event.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusSeconds(10));

        service.sendEvent(event);

        // Aguarda a execução assíncrona
        Thread.sleep(500);

        verify(kafkaProducer).send(anyString(), eq(event.getEventJson()), anyString());
        verify(metricsCollector).incrementKafkaEventProduced();
        verify(outboxEventRepository).delete(event);
    }

    @Test
    void testSendEventShouldSkipExpiredEvent() throws InterruptedException {
        event.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(5));

        service.sendEvent(event);

        Thread.sleep(500);

        verify(outboxEventRepository).delete(event);
        verify(kafkaProducer, never()).send(any(), any(), any());
        verify(metricsCollector, never()).incrementKafkaEventProduced();
    }

    @Test
    void testSendEventShouldCallInitAndSendEventForEach() {
        List<OutboxEvent> events = List.of(event);
        when(outboxEventRepository.findByEventsPendingProcessing(any(), any())).thenReturn(events);

        EventSubmissionService spyService = Mockito.spy(service);
        doNothing().when(spyService).sendEvent(any());

        spyService.sendEvent();

        verify(spyService).init(event);
        verify(spyService).sendEvent(event);
    }
}