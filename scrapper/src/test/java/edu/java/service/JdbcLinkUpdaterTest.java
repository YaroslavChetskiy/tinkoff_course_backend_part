package edu.java.service;

import edu.java.client.bot.BotClient;
import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.dto.request.LinkUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static edu.java.dto.entity.LinkType.GITHUB_REPO;
import static edu.java.dto.entity.LinkType.STACKOVERFLOW_QUESTION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcLinkUpdaterTest {

    @Mock
    private JdbcChatLinkRepository chatLinkRepository;

    @Mock
    private JdbcLinkRepository linkRepository;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private GithubClient githubClient;

    @Mock
    private BotClient botClient;

    @InjectMocks
    private JdbcLinkUpdater jdbcLinkUpdater;

    @Test
    public void testUpdate() {
        List<Link> outdatedLinks = Arrays.asList(
            new Link(
                1L,
                "https://github.com/example/repo",
                GITHUB_REPO,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(2),
                null
            ),
            new Link(
                2L,
                "https://stackoverflow.com/questions/12345/sample-question",
                STACKOVERFLOW_QUESTION,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(2),
                null
            )
        );

        when(linkRepository.findOutdatedLinks(any(Duration.class))).thenReturn(outdatedLinks);
        when(githubClient.checkForUpdate(any(Link.class))).thenReturn(OffsetDateTime.now());
        when(stackOverflowClient.checkForUpdate(any(Link.class))).thenReturn(OffsetDateTime.now());

        int updatedCount = jdbcLinkUpdater.update();

        assertEquals(2, updatedCount);

        verify(botClient, times(2)).sendUpdate(any(LinkUpdateRequest.class));
        verify(chatLinkRepository, times(2)).findAllChatIdsByLinkId(anyLong());
        verify(linkRepository, times(2)).updateLastUpdateAndCheckTime(
            anyString(),
            any(OffsetDateTime.class),
            any(OffsetDateTime.class)
        );
    }
}
