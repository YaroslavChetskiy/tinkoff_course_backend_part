package edu.java.configuration.access;

import edu.java.client.bot.BotClient;
import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jooq.JooqChatLinkRepository;
import edu.java.domain.repository.jooq.JooqChatRepository;
import edu.java.domain.repository.jooq.JooqLinkRepository;
import edu.java.domain.repository.jooq.JooqQuestionRepository;
import edu.java.service.ChatService;
import edu.java.service.LinkService;
import edu.java.service.LinkUpdater;
import edu.java.service.jooq.JooqChatService;
import edu.java.service.jooq.JooqLinkService;
import edu.java.service.jooq.JooqLinkUpdater;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jooq")
public class JooqAccessConfiguration {

    @Bean
    public LinkService linkService(
        JooqLinkRepository linkRepository,
        JooqChatLinkRepository chatLinkRepository,
        JooqQuestionRepository questionRepository,
        StackOverflowClient stackOverflowClient
    ) {
        return new JooqLinkService(
            linkRepository,
            chatLinkRepository,
            questionRepository,
            stackOverflowClient
        );
    }

    @Bean
    public ChatService chatService(JooqChatRepository chatRepository) {
        return new JooqChatService(chatRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(
        JooqChatLinkRepository chatLinkRepository,
        JooqLinkRepository linkRepository,
        JooqQuestionRepository questionRepository,
        StackOverflowClient stackOverflowClient,
        GithubClient githubClient,
        BotClient botClient
    ) {
        return new JooqLinkUpdater(
            chatLinkRepository,
            linkRepository,
            questionRepository,
            stackOverflowClient,
            githubClient,
            botClient
        );
    }

}
