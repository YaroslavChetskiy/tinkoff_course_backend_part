package edu.java.configuration;

import edu.java.client.github.GithubClient;
import edu.java.client.github.GithubWebClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.client.stackoverflow.StackOverflowWebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

    @Bean
    public GithubClient githubClient() {
        return new GithubWebClient();
    }

    @Bean
    public StackOverflowClient stackOverflowClient() {
        return new StackOverflowWebClient();
    }
}
