package edu.java.bot.configuration.kafka;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka", ignoreUnknownFields = false)
public record KafkaProperties(
    @Value("kafka.group-id") String groupId,
    @Value("bootstrap-servers") String bootstrapServers,
    @Value("kafka.auto-offset-reset") String autoOffsetReset,
    @Value("kafka.max-poll-interval-ms") Integer maxPollIntervalMs,
    @Value("kafka.enable-auto-commit") Boolean enableAutoCommit,
    @Value("kafka.concurrency") Integer concurrency,
    @NotNull
    @Value("kafka.dlq") DLQProperties dlq
) {

    public record DLQProperties(
        @Value("kafka.dlq.topic") String topic,
        @Value("kafka.dlq.replication-factor") Integer replicationFactor,
        @Value("kafka.dlq.partitions") Integer partitions,
        @Value("kafka.dlq.acks-mode") String acksMode,
        @Value("kafka.dlq.delivery-timeout") Integer deliveryTimeout,
        @Value("kafka.dlq.linger-ms") Integer lingerMs,
        @Value("kafka.dlq.batch-size") Integer batchSize,
        @Value("kafka.dlq.max-in-flight-per-connection") Integer maxInFlightPerConnection
    ) {

    }
}
