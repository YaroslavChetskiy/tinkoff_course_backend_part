package edu.java.configuration.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(
    @Value("kafka.topic") String topic,
    @Value("kafka.bootstrap-servers") String bootstrapServers,
    @Value("kafka.replication-factor") short replicationFactor,
    @Value("kafka.partitions") int partitions,
    @Value("kafka.acks-mode") String acksMode,
    @Value("kafka.delivery-timeout") Integer deliveryTimeout,
    @Value("kafka.linger-ms") Integer lingerMs,
    @Value("kafka.batch-size") Integer batchSize,
    @Value("kafka.max-in-flight-per-connection") Integer maxInFlightPerConnection,
    @Value("kafka.enable-idempotence") Boolean enableIdempotence
) {
}
