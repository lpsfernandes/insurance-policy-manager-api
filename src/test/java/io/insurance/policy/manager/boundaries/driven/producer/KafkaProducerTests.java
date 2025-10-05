package io.insurance.policy.manager.boundaries.driven.producer;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaProducer kafkaProducer;

    private final String topicName = "test-topic";

    @BeforeEach
    void setUp() {
        kafkaProducer = new KafkaProducer(topicName, kafkaTemplate);
    }

    @Test
    void testSendShouldPublishToKafkaTemplate() {
        String header = "trace-header";
        String message = "{\"data\":\"test\"}";

        kafkaProducer.send(header, message);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(captor.capture());

        ProducerRecord<String, String> record = captor.getValue();
        assertEquals(topicName, record.topic());
        assertEquals(header, record.key());
        assertEquals(message, record.value());
    }

}