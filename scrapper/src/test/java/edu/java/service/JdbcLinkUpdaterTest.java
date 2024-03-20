package edu.java.service;

import edu.java.client.bot.BotClient;
import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.domain.repository.jdbc.JdbcQuestionRepository;
import edu.java.dto.entity.jdbc.Link;
import edu.java.dto.entity.jdbc.Question;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.stackoverflow.QuestionResponse.ItemResponse;
import edu.java.dto.update.UpdateInfo;
import edu.java.service.jdbc.JdbcLinkUpdater;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static edu.java.dto.entity.jdbc.LinkType.GITHUB_REPO;
import static edu.java.dto.entity.jdbc.LinkType.STACKOVERFLOW_QUESTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcLinkUpdaterTest {

    @Mock
    private JdbcChatLinkRepository chatLinkRepository;

    @Mock JdbcQuestionRepository questionRepository;

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

        var updateInfo = new UpdateInfo(
            true,
            OffsetDateTime.now(),
            "dummy"
        );

        var question = new Question(1L, 2, 2L);

        when(linkRepository.findOutdatedLinks(any(Duration.class))).thenReturn(outdatedLinks);

        when(githubClient.checkForUpdate(any(String.class), any(OffsetDateTime.class))).thenReturn(updateInfo
        );

        when(questionRepository.findByLinkId(anyLong())).thenReturn(question);

        when(stackOverflowClient.checkForUpdate(any(String.class), any(OffsetDateTime.class), anyInt(),
            any(ItemResponse.class)
        ))
            .thenReturn(updateInfo);

        when(stackOverflowClient.fetchQuestion(anyLong())).thenReturn(
            new QuestionResponse(
                List.of(
                    new ItemResponse(
                        1L,
                        "https://stackoverflow.com/questions/12345/sample-question",
                        OffsetDateTime.now(),
                        3
                    )
                )
            )
        );

        int updatedCount = jdbcLinkUpdater.update();

        assertEquals(2, updatedCount);

        verify(botClient, times(2)).sendUpdate(any(LinkUpdateRequest.class));
        verify(chatLinkRepository, times(2)).findAllChatIdsByLinkId(anyLong());
        verify(linkRepository, times(2)).updateLastUpdateAndCheckTime(
            anyString(),
            any(OffsetDateTime.class),
            any(OffsetDateTime.class)
        );
        verify(questionRepository, times(1)).findByLinkId(anyLong());
        verify(questionRepository, times(1)).updateAnswerCountByLinkId(anyLong(), anyInt());
    }
}
