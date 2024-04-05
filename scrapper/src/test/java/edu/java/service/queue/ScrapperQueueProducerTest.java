package edu.java.service.queue;

import edu.java.configuration.kafka.KafkaProperties;
import edu.java.dto.request.LinkUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrapperQueueProducerTest {

    public static final String TEST_TOPIC = "test-topic";
    @Mock
    private KafkaTemplate<Integer, LinkUpdateRequest> kafkaTemplate;

    @Mock
    private KafkaProperties kafkaProperties;

    @InjectMocks
    private ScrapperQueueProducer scrapperQueueProducer;

    @Test
    void sendUpdate() {
        var linkUpdateRequest = new LinkUpdateRequest(1L, "dummy", "dummy", List.of());

        when(kafkaProperties.topic()).thenReturn(TEST_TOPIC);

        scrapperQueueProducer.sendUpdate(linkUpdateRequest);

        verify(kafkaTemplate, times(1)).send(TEST_TOPIC, linkUpdateRequest);
    }

}
