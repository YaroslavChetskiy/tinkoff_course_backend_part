package edu.java.bot.listener;

import edu.java.bot.model.dto.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@RequiredArgsConstructor
@Validated
public class ScrapperQueueListener {

    private final BotService botService;

    @KafkaListener(topics = "${app.scrapper-topic.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listenUpdateMessage(
        @Payload @Valid LinkUpdateRequest updateRequest,
        Acknowledgment acknowledgment
    ) {
        log.info("[Listener] New update arrived: {}", updateRequest);
        try {
            botService.sendUpdate(updateRequest);
        } catch (Exception exception) { // на случай непредвиденных ошибок при обработке сообщения
            log.info("[Listener] Error in update message handling: ", exception);
            throw exception;
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
