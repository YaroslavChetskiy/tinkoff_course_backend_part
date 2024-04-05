package edu.java.bot.listener;

import edu.java.bot.model.dto.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScrapperQueueListenerTest {

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private BotService botService;

    @InjectMocks
    private ScrapperQueueListener scrapperQueueListener;

    @Test
    void listenValidUpdateMessage() {
        var linkUpdateRequest = new LinkUpdateRequest(1L, "dummy", "dummy", List.of());

        scrapperQueueListener.listenUpdateMessage(linkUpdateRequest, acknowledgment);

        verify(botService, times(1)).sendUpdate(linkUpdateRequest);

        verify(acknowledgment, times(1)).acknowledge();
    }
}
