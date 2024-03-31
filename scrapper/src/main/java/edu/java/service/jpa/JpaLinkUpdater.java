package edu.java.service.jpa;

import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jpa.JpaChatLinkRepository;
import edu.java.domain.repository.jpa.JpaLinkRepository;
import edu.java.dto.entity.hibernate.link.Link;
import edu.java.dto.entity.hibernate.link.StackOverflowQuestionLink;
import edu.java.dto.entity.jdbc.LinkType;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.update.UpdateInfo;
import edu.java.service.LinkUpdater;
import edu.java.service.notification.NotificationService;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class JpaLinkUpdater implements LinkUpdater {

    private static final Duration THRESHOLD = Duration.ofDays(1L);

    private final JpaChatLinkRepository chatLinkRepository;
    private final JpaLinkRepository linkRepository;
    private final StackOverflowClient stackOverflowClient;
    private final GithubClient githubClient;
    //    private final BotClient botClient;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public int update() {
        int updatedCount = 0;

        List<Link> outdatedLinks = linkRepository.findOutdatedLinks(OffsetDateTime.now().minus(THRESHOLD));

        for (Link link : outdatedLinks) {
            UpdateInfo updateInfo;

            if (LinkType.resolve(link.getUrl()) == LinkType.GITHUB_REPO) {
                updateInfo = githubClient.checkForUpdate(link.getUrl(), link.getLastUpdatedAt());
            } else {
                QuestionResponse.ItemResponse question = stackOverflowClient
                    .fetchQuestion(stackOverflowClient.getQuestionId(link.getUrl()))
                    .items().getFirst();

                updateInfo = stackOverflowClient.checkForUpdate(
                    link.getUrl(),
                    link.getLastUpdatedAt(),
                    ((StackOverflowQuestionLink) link).getAnswerCount(),
                    question
                );
                if (updateInfo.isNewUpdate()) {
                    linkRepository.updateAnswerCountByUrl(link.getUrl(), question.answerCount());
                }
            }

            if (updateInfo.isNewUpdate()) {
                log.info("SEND UPDATE");
                notificationService.sendUpdateNotification(new LinkUpdateRequest(
                        link.getId(),
                        link.getUrl(),
                        updateInfo.message(),
                        chatLinkRepository.findAllChatIdsByLinkId(link.getId())
                    )
                );
                updatedCount++;
            }

            linkRepository.updateLastUpdatedAtAndCheckedAtByUrl(
                link.getUrl(),
                updateInfo.updateTime(),
                OffsetDateTime.now()
            );
        }

        return updatedCount;
    }
}
