package edu.java.bot.configuration.retry;

import edu.java.bot.configuration.retry.backoff.ConstantBackoff;
import edu.java.bot.configuration.retry.backoff.ExponentialBackoff;
import edu.java.bot.configuration.retry.backoff.LinearBackoff;
import edu.java.bot.configuration.retry.backoff.RetryBackoff;
import edu.java.bot.configuration.retry.policy.RetryPolicy;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RetryConfigProperties.class)
public class RetryConfiguration {

    private final RetryConfigProperties configProperties;

    @Bean
    public RetryPolicy retryPolicy(RetryBackoff backOff) {

        return new RetryPolicy(
            configProperties.codes().stream().map(HttpStatusCode::valueOf).toList(),
            backOff
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "client.retry", name = "back-off-type", havingValue = "constant")
    public RetryBackoff constantBackOff() {
        return new ConstantBackoff(configProperties.delay());
    }

    @Bean
    @ConditionalOnProperty(prefix = "client.retry", name = "back-off-type", havingValue = "linear")
    public RetryBackoff linearBackOff() {
        return new LinearBackoff(configProperties.delay(), configProperties.maxDelay(), configProperties.increment());
    }

    @Bean
    @ConditionalOnProperty(prefix = "client.retry", name = "back-off-type", havingValue = "exponential")
    public RetryBackoff exponentialBackOff() {
        return new ExponentialBackoff(
            configProperties.delay(),
            configProperties.maxDelay(),
            configProperties.multiplier()
        );
    }

    @Bean
    public ExchangeFilterFunction retryFilterFunction(RetryPolicy retryPolicy) {

        return new RetryExchangeFilterFunction(configProperties.maxAttempts(), retryPolicy);
    }

    public static class RetryExchangeFilterFunction implements ExchangeFilterFunction {

        private final int maxAttempts;
        private final RetryPolicy retryPolicy;

        public RetryExchangeFilterFunction(int maxAttempts, RetryPolicy retryPolicy) {
            this.maxAttempts = maxAttempts;
            this.retryPolicy = retryPolicy;
        }

        @Override
        public @NotNull Mono<ClientResponse> filter(@NotNull ClientRequest request, @NotNull ExchangeFunction next) {
            return retry(request, next, 1);
        }

        private Mono<ClientResponse> retry(ClientRequest request, ExchangeFunction next, int attempt) {
            return next.exchange(request)
                .flatMap(response -> {
                    if (retryPolicy.retryableStatuses().contains(response.statusCode()) && attempt <= maxAttempts) {
                        Duration delay = retryPolicy.retryBackoff().calculateDelay(attempt);
                        return Mono.delay(delay)
                            .then(Mono.defer(() -> retry(request, next, attempt + 1)));
                    }
                    return Mono.just(response);
                });
        }
    }
}
