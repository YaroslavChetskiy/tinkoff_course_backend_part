package edu.java.service.jdbc;

import edu.java.client.bot.BotClient;
import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.domain.repository.jdbc.JdbcQuestionRepository;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.Question;
import edu.java.dto.request.LinkUpdateRequest;
import edu.java.dto.update.UpdateInfo;
import edu.java.service.LinkUpdater;
import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static edu.java.dto.entity.LinkType.GITHUB_REPO;
import static edu.java.dto.entity.LinkType.STACKOVERFLOW_QUESTION;

@Service
@RequiredArgsConstructor
public class JdbcLinkUpdater implements LinkUpdater {

    private static final Duration THRESHOLD = Duration.ofDays(1L);

    private final JdbcChatLinkRepository chatLinkRepository;
    private final JdbcLinkRepository linkRepository;
    private final JdbcQuestionRepository questionRepository;
    private final StackOverflowClient stackOverflowClient;
    private final GithubClient githubClient;
    private final BotClient botClient;

    @Transactional
    @Override
    public int update() {
        int updatedCount = 0;

        var outdatedLinks = linkRepository.findOutdatedLinks(THRESHOLD);

        for (Link link : outdatedLinks) {
            UpdateInfo updateInfo = new UpdateInfo(false, link.getLastUpdatedAt(), "Обновлений нет");

            if (link.getType() == GITHUB_REPO) {
                updateInfo = githubClient.checkForUpdate(link);
            } else if (link.getType() == STACKOVERFLOW_QUESTION) {
                Question question = questionRepository.findByLinkId(link.getId());
                updateInfo = stackOverflowClient.checkForUpdate(link, question.getAnswerCount());
            }

            if (updateInfo.isNewUpdate()) {
                botClient.sendUpdate(new LinkUpdateRequest(
                        link.getId(),
                        link.getUrl(),
                        updateInfo.message(),
                        chatLinkRepository.findAllChatIdsByLinkId(link.getId())
                    )
                );
                updatedCount++;
            }

            linkRepository.updateLastUpdateAndCheckTime(link.getUrl(), updateInfo.updateTime(), OffsetDateTime.now());
        }

        return updatedCount;
    }
}