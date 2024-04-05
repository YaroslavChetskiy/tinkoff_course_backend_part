package edu.java.service.jpa;

import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jpa.JpaChatLinkRepository;
import edu.java.domain.repository.jpa.JpaLinkRepository;
import edu.java.dto.entity.hibernate.link.GithubRepositoryLink;
import edu.java.dto.entity.hibernate.link.Link;
import edu.java.dto.entity.hibernate.link.StackOverflowQuestionLink;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.stackoverflow.QuestionResponse.ItemResponse;
import edu.java.dto.update.UpdateInfo;
import edu.java.service.notification.NotificationService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaLinkUpdaterTest {

    @Mock
    private JpaChatLinkRepository chatLinkRepository;

    @Mock
    private JpaLinkRepository linkRepository;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private GithubClient githubClient;

//    @Mock
//    private BotClient botClient;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private JpaLinkUpdater linkUpdater;

    @Test
    public void update() {
        List<Link> outdatedLinks = Arrays.asList(
            GithubRepositoryLink.builder()
                .id(1L)
                .url("https://github.com/example/repo")
                .lastUpdatedAt(OffsetDateTime.now().minusDays(2))
                .checkedAt(OffsetDateTime.now().minusDays(2))
                .chatLinks(new ArrayList<>())
                .build()
            ,
            StackOverflowQuestionLink.builder()
                .id(2L)
                .url("https://stackoverflow.com/questions/12345/sample-question")
                .lastUpdatedAt(OffsetDateTime.now().minusDays(2))
                .checkedAt(OffsetDateTime.now().minusDays(2))
                .chatLinks(new ArrayList<>())
                .answerCount(2)
                .build()
        );

        var updateInfo = new UpdateInfo(
            true,
            OffsetDateTime.now(),
            "dummy"
        );

        when(linkRepository.findOutdatedLinks(any(OffsetDateTime.class))).thenReturn(outdatedLinks);

        when(githubClient.checkForUpdate(any(String.class), any(OffsetDateTime.class))).thenReturn(updateInfo);

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

        int updatedCount = linkUpdater.update();

        assertEquals(2, updatedCount);

        verify(notificationService, times(2)).sendUpdateNotification(any(LinkUpdateRequest.class));
        verify(chatLinkRepository, times(2)).findAllChatIdsByLinkId(anyLong());
        verify(linkRepository, times(2)).updateLastUpdatedAtAndCheckedAtByUrl(
            anyString(),
            any(OffsetDateTime.class),
            any(OffsetDateTime.class)
        );
        verify(linkRepository, times(1)).updateAnswerCountByUrl(anyString(), anyInt());
    }
}
