package edu.java.bot.configuration.retry.backoff;

import java.time.Duration;

public record ExponentialBackoff(Duration initialDelay, Duration maxDelay, double multiplier) implements RetryBackoff {
    @Override
    public Duration calculateDelay(int attempts) {
        Duration backoff = initialDelay.multipliedBy((long) Math.pow(multiplier, attempts - 1));
        return backoff.compareTo(maxDelay) < 0 ? backoff : maxDelay;
    }
}
