package edu.java.service.queue;

import edu.java.configuration.kafka.KafkaProperties;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.scrapper.IntegrationKafkaTest;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
public class IntegrationScrapperQueueProducerTest extends IntegrationKafkaTest {

    @Autowired
    private KafkaTemplate<Integer, LinkUpdateRequest> kafkaTemplate;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Test
    void sendUpdate() {
        Consumer<Integer, LinkUpdateRequest> consumer = new KafkaConsumer<>(getConsumerProps());

        consumer.subscribe(Collections.singletonList(kafkaProperties.topic()));

        LinkUpdateRequest updateRequest =
            new LinkUpdateRequest(1L, "dummy.com", "dummy", List.of(123L));

        ScrapperQueueProducer producer = new ScrapperQueueProducer(kafkaTemplate, kafkaProperties);

        producer.sendUpdate(updateRequest);
        Unreliables.retryUntilTrue(20, TimeUnit.SECONDS, () -> {
            ConsumerRecords<Integer, LinkUpdateRequest> records = consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                return false;
            }

            for (ConsumerRecord<Integer, LinkUpdateRequest> record : records) {
                assertThat(record.value()).isEqualTo(updateRequest);
            }

            return true;
        });

        consumer.close();
    }

    private Map<String, Object> getConsumerProps() {
        Map<String, Object> props =
            KafkaTestUtils.consumerProps(kafkaProperties.bootstrapServers(), "test-group", "false");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            JsonDeserializer.class
        );
        props.put(
            JsonDeserializer.VALUE_DEFAULT_TYPE,
            LinkUpdateRequest.class
        );
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }
}
