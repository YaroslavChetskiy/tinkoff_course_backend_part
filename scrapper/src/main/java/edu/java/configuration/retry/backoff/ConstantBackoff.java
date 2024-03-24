package edu.java.configuration.retry.backoff;

import java.time.Duration;

public record ConstantBackoff(Duration delay) implements RetryBackoff {
    @Override
    public Duration calculateDelay(int attempts) {
        return delay;
    }
}
