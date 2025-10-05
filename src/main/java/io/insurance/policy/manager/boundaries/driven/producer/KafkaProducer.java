package io.insurance.policy.manager.boundaries.driven.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducer {

    private final String topicName;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(@Value("${spring.kafka.producer.topic}") String topicName,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.topicName = topicName;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String header, String message){
        log.debug("Enviando payload: [{}] para o topico [{}]", message, topicName);
        kafkaTemplate.send(new ProducerRecord<>(topicName, header, message));
    }

}
