package edu.java.configuration;

import edu.java.client.github.GithubClient;
import edu.java.client.github.GithubWebClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.client.stackoverflow.StackOverflowWebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Configuration
public class ClientConfiguration {

    @Bean
    public GithubClient githubClient(ExchangeFilterFunction filterFunction) {
        return new GithubWebClient(filterFunction);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(ExchangeFilterFunction filterFunction) {
        return new StackOverflowWebClient(filterFunction);
    }
}
