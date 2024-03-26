package edu.java.bot.configuration.retry;

import java.time.Duration;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.retry", ignoreUnknownFields = false)
public record RetryConfigProperties(
    @Value("client.retry.max-attempts") Integer maxAttempts,
    @Value("client.retry.back-off-type") String backOffType,
    @Value("client.retry.increment") Duration increment,
    @Value("client.retry.delay") Duration delay,
    @Value("client.retry.max-delay") Duration maxDelay,
    @Value("client.retry.multiplier") Double multiplier,
    @Value("client.retry.codes") Set<Integer> codes
) {
}
