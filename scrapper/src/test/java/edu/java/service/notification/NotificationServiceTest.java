package edu.java.service.notification;

import edu.java.client.bot.BotClient;
import edu.java.configuration.ApplicationConfig;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.service.queue.ScrapperQueueProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final LinkUpdateRequest REQUEST = new LinkUpdateRequest(
        1L,
        "dummy",
        "dummy",
        List.of()
    );

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private ScrapperQueueProducer queueProducer;

    @Mock
    private BotClient botClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendUpdateNotificationIfUseQueue() {
        when(applicationConfig.useQueue()).thenReturn(true);

        notificationService.sendUpdateNotification(REQUEST);

        verify(queueProducer, times(1)).sendUpdate(REQUEST);
    }

    @Test
    void sendUpdateNotificationIfNotUseQueue() {
        when(applicationConfig.useQueue()).thenReturn(false);

        notificationService.sendUpdateNotification(REQUEST);

        verify(botClient, times(1)).sendUpdate(REQUEST);
    }
}
