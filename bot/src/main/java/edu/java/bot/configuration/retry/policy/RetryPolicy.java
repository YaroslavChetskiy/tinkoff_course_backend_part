package edu.java.bot.configuration.retry.policy;

import edu.java.bot.configuration.retry.backoff.RetryBackoff;
import java.util.List;
import org.springframework.http.HttpStatusCode;

public record RetryPolicy(List<HttpStatusCode> retryableStatuses, RetryBackoff retryBackoff) {
}
