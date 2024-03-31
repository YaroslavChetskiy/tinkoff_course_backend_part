package edu.java.configuration.access;

import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jpa.JpaChatLinkRepository;
import edu.java.domain.repository.jpa.JpaChatRepository;
import edu.java.domain.repository.jpa.JpaLinkRepository;
import edu.java.service.ChatService;
import edu.java.service.LinkService;
import edu.java.service.LinkUpdater;
import edu.java.service.jpa.JpaChatService;
import edu.java.service.jpa.JpaLinkService;
import edu.java.service.jpa.JpaLinkUpdater;
import edu.java.service.notification.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaAccessConfiguration {

    @Bean
    public LinkService linkService(
        JpaLinkRepository linkRepository,
        JpaChatRepository chatRepository,
        JpaChatLinkRepository chatLinkRepository,
        StackOverflowClient stackOverflowClient
    ) {
        return new JpaLinkService(
            linkRepository,
            chatRepository,
            chatLinkRepository,
            stackOverflowClient
        );
    }

    @Bean
    public ChatService chatService(JpaChatRepository chatRepository) {
        return new JpaChatService(chatRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(
        JpaChatLinkRepository chatLinkRepository,
        JpaLinkRepository linkRepository,
        StackOverflowClient stackOverflowClient,
        GithubClient githubClient,
        NotificationService notificationService
    ) {
        return new JpaLinkUpdater(
            chatLinkRepository,
            linkRepository,
            stackOverflowClient,
            githubClient,
            notificationService
        );
    }
}
