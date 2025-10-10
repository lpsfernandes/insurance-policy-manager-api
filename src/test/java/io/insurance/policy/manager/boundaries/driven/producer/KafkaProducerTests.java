package io.insurance.policy.manager.boundaries.driven.producer;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class KafkaProducerTests {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducer kafkaProducer;

    @Captor
    private ArgumentCaptor<ProducerRecord<String, String>> recordCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSendMessageToKafkaTopic() {
        String header = "event-header";
        String message = "{\"id\":\"123\",\"status\":\"APPROVED\"}";
        String topic = "payments-topic";

        kafkaProducer.send(header, message, topic);

        verify(kafkaTemplate).send(recordCaptor.capture());

        ProducerRecord<String, String> record = recordCaptor.getValue();
        assertEquals(topic, record.topic());
        assertEquals(header, record.key());
        assertEquals(message, record.value());
    }
}