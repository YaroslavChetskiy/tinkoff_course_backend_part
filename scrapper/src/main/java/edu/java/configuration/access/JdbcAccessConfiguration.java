package edu.java.configuration.access;

import edu.java.client.bot.BotClient;
import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcChatRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.domain.repository.jdbc.JdbcQuestionRepository;
import edu.java.service.ChatService;
import edu.java.service.LinkService;
import edu.java.service.LinkUpdater;
import edu.java.service.jdbc.JdbcChatService;
import edu.java.service.jdbc.JdbcLinkService;
import edu.java.service.jdbc.JdbcLinkUpdater;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
public class JdbcAccessConfiguration {

    @Bean
    public LinkService linkService(
        JdbcLinkRepository linkRepository,
        JdbcChatLinkRepository chatLinkRepository,
        JdbcQuestionRepository questionRepository,
        StackOverflowClient stackOverflowClient
    ) {
        return new JdbcLinkService(
            linkRepository,
            chatLinkRepository,
            questionRepository,
            stackOverflowClient
        );
    }

    @Bean
    public ChatService chatService(JdbcChatRepository chatRepository) {
        return new JdbcChatService(chatRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(
        JdbcChatLinkRepository chatLinkRepository,
        JdbcLinkRepository linkRepository,
        JdbcQuestionRepository questionRepository,
        StackOverflowClient stackOverflowClient,
        GithubClient githubClient,
        BotClient botClient
    ) {
        return new JdbcLinkUpdater(
            chatLinkRepository,
            linkRepository,
            questionRepository,
            stackOverflowClient,
            githubClient,
            botClient
        );
    }
}
