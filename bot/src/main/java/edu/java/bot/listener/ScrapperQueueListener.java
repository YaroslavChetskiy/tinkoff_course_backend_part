package edu.java.bot.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.configuration.kafka.KafkaProperties;
import edu.java.bot.model.dto.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapperQueueListener {

    private final KafkaTemplate<Integer, byte[]> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final BotService botService;
    private final Validator validator;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.scrapper-topic.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listenUpdateMessage(@Payload LinkUpdateRequest updateRequest, Acknowledgment acknowledgment) {
        log.info("[Listener] New update arrived: {}", updateRequest);
        try {
            if (validateUpdateMessage(updateRequest)) {
                botService.sendUpdate(updateRequest);
            } else {
                log.info("[Listener] Update message is invalid: {}", updateRequest);
                sendToDLQ(updateRequest);
            }
        } catch (Exception exception) { // на случай непредвиденных ошибок при обработке сообщения
            log.info("[Listener] Error in update message handling: ", exception);
            sendToDLQ(updateRequest);
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private boolean validateUpdateMessage(LinkUpdateRequest updateRequest) {
        Set<ConstraintViolation<LinkUpdateRequest>> violations = validator.validate(updateRequest);
        return violations.isEmpty();
    }

    private void sendToDLQ(LinkUpdateRequest updateRequest) {
        try {
            log.info("[DLQ] Send update message to DLQ: {}", updateRequest);
            kafkaTemplate.send(kafkaProperties.dlq().topic(), objectMapper.writeValueAsBytes(updateRequest));
        } catch (Exception exception) {
            log.error("[DLQ] Error occurred during sending to Kafka: ", exception);
        }
    }
}
