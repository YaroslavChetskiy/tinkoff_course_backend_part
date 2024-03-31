package edu.java.bot.listener;

import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.configuration.kafka.KafkaProperties;
import edu.java.bot.kafka.IntegrationKafkaTest;
import edu.java.bot.model.dto.request.LinkUpdateRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
public class IntegrationScrapperQueueListenerTest extends IntegrationKafkaTest {

    private static final LinkUpdateRequest REQUEST = new LinkUpdateRequest(1L, "", "", List.of());

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ApplicationConfig applicationConfig;

    @BeforeEach
    public void setup() {
        createTopic(kafkaProperties.dlq().topic());
    }

    private void createTopic(String topicName) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        try (AdminClient adminClient = AdminClient.create(properties)) {
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create topic " + topicName, e);
        }
    }

    @Test
    @Disabled // если запускать в одиночку этот тест, он проходит, если все вместе
        // - TimeOutException (нужно больше 2-х минут ждать)
    void listenInvalidUpdateMessage() {

        KafkaProducer<Integer, String> stringKafkaProducer = new KafkaProducer<>(getProducerProps());

        stringKafkaProducer.send(
            new ProducerRecord<>(applicationConfig.scrapperTopic().name(), 1, REQUEST.toString())
        );

        Consumer<Integer, byte[]> consumerDLQ = new KafkaConsumer<>(getDLQConsumerProps());

        consumerDLQ.subscribe(Collections.singletonList(kafkaProperties.dlq().topic()));

        Unreliables.retryUntilTrue(120, TimeUnit.SECONDS, () -> {
            ConsumerRecords<Integer, byte[]> records = consumerDLQ.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                return false;
            }

            for (ConsumerRecord<Integer, byte[]> record : records) {
                String dlqMessage = new String(record.value(), StandardCharsets.UTF_8);
                assertThat(dlqMessage).isEqualTo(REQUEST.toString());
            }

            return true;
        });

        consumerDLQ.close();
    }

    private Map<String, Object> getDLQConsumerProps() {
        Map<String, Object> props =
            KafkaTestUtils.consumerProps(kafkaProperties.bootstrapServers(), "test-group", "false");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return props;
    }

    private Map<String, Object> getProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

}
