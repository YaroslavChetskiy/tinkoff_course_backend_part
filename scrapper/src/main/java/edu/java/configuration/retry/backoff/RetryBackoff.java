package edu.java.configuration.retry.backoff;

import java.time.Duration;

public interface RetryBackoff {

    Duration calculateDelay(int attempts);
}
