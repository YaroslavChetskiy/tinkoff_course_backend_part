package edu.java.configuration.retry.backoff;

import java.time.Duration;

public record LinearBackoff(Duration initialDelay, Duration maxDelay, Duration increment) implements RetryBackoff {

    @Override
    public Duration calculateDelay(int attempts) {
        Duration backoff = initialDelay.plus(increment.multipliedBy(attempts - 1));
        return backoff.compareTo(maxDelay) < 0 ? backoff : maxDelay;
    }
}
