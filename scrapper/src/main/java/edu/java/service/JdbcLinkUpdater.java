package edu.java.service;

import edu.java.client.bot.BotClient;
import edu.java.client.github.GithubClient;
import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.dto.entity.Link;
import edu.java.dto.request.LinkUpdateRequest;
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
    private final StackOverflowClient stackOverflowClient;
    private final GithubClient githubClient;
    private final BotClient botClient;

    @Transactional
    @Override
    public int update() {
        int updatedCount = 0;

        var outdatedLinks = linkRepository.findOutdatedLinks(THRESHOLD);

        for (Link link : outdatedLinks) {
            OffsetDateTime lastUpdatedAt = link.getLastUpdatedAt();
            if (link.getType() == GITHUB_REPO) {
                lastUpdatedAt = githubClient.checkForUpdate(link);
            } else if (link.getType() == STACKOVERFLOW_QUESTION) {
                lastUpdatedAt = stackOverflowClient.checkForUpdate(link);
            }

            if (lastUpdatedAt.isAfter(link.getLastUpdatedAt())) {
                botClient.sendUpdate(new LinkUpdateRequest(
                        link.getId(),
                        link.getUrl(),
                        "По ссылке есть обновление",
                        chatLinkRepository.findAllChatIdsByLinkId(link.getId())
                    )
                );
                updatedCount++;
            }

            linkRepository.updateLastUpdateAndCheckTime(link.getUrl(), lastUpdatedAt, OffsetDateTime.now());
        }

        return updatedCount;
    }
}
