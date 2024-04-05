package edu.java.service.notification;

import edu.java.client.bot.BotClient;
import edu.java.configuration.ApplicationConfig;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.service.queue.ScrapperQueueProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationConfig applicationConfig;
    private final ScrapperQueueProducer queueProducer;
    private final BotClient botClient;

    public void sendUpdateNotification(LinkUpdateRequest updateRequest) {
        if (applicationConfig.useQueue()) {
            queueProducer.sendUpdate(updateRequest);
        } else {
            botClient.sendUpdate(updateRequest);
        }
    }
}
