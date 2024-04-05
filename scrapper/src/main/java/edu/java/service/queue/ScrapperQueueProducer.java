package edu.java.service.queue;

import edu.java.configuration.kafka.KafkaProperties;
import edu.java.dto.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScrapperQueueProducer {

    private final KafkaTemplate<Integer, LinkUpdateRequest> kafkaTemplate;

    private final KafkaProperties kafkaProperties;

    public void sendUpdate(LinkUpdateRequest updateRequest) {
        try {
            kafkaTemplate.send(kafkaProperties.topic(), updateRequest);
        } catch (Exception exception) {
            log.error("Error occurred during sending to Kafka: ", exception);
        }
    }
}
