package io.insurance.policy.manager.boundaries.driven.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String header, String message, String topic) {
        log.debug("Enviando payload: [{}] para o topico [{}]", message, topic);
        kafkaTemplate.send(new ProducerRecord<>(topic, header, message));
    }

}
