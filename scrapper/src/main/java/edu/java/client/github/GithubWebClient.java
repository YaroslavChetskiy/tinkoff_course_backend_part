package edu.java.client.github;

import edu.java.dto.github.EventResponse;
import edu.java.dto.github.RepositoryResponse;
import edu.java.dto.update.UpdateInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.web.reactive.function.client.WebClient;
import static edu.java.dto.github.EventType.UNKNOWN;

public class GithubWebClient implements GithubClient {

    private static final String DEFAULT_BASE_URL = "https://api.github.com/";

    private static final String REPOSITORY_ENDPOINT = "/repos/{owner}/{repo}";
    private static final String EVENTS_ENDPOINT = "/repos/{owner}/{repo}/events";

    private final WebClient webClient;

    public GithubWebClient() {
        this(DEFAULT_BASE_URL);
    }

    public GithubWebClient(String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public RepositoryResponse fetchRepository(String owner, String repository) {
        return webClient.get()
            .uri(REPOSITORY_ENDPOINT, owner, repository)
            .retrieve()
            .bodyToMono(RepositoryResponse.class)
            .block();
    }

    @Override
    public List<EventResponse> fetchEvents(String owner, String repository) {
        return Arrays.stream(Objects.requireNonNull(webClient.get()
            .uri(EVENTS_ENDPOINT, owner, repository)
            .retrieve()
            .bodyToMono(EventResponse[].class)
            .block())).toList();
    }

    @Override
    public UpdateInfo checkForUpdate(String url, OffsetDateTime lastUpdatedAt) {
        try {
            URI uri = new URI(url);
            String[] pathParts = uri.getPath().split("/");

            var owner = pathParts[1];
            var repository = pathParts[2];

            RepositoryResponse response = fetchRepository(owner, repository);

            if (response.lastPushedTime().isAfter(lastUpdatedAt)) {
                EventResponse lastEvent = fetchEvents(owner, repository)
                    .stream()
                    .max(Comparator.comparing(EventResponse::createdAt))
                    .orElse(null);

                // к сожалению, я не смог понять, всегда ли pushed_at относится к времени последнего события
                if (lastEvent != null && lastEvent.createdAt().isAfter(lastUpdatedAt)) {
                    return new UpdateInfo(
                        true,
                        lastEvent.createdAt(),
                        lastEvent.type().generateUpdateMessage(lastEvent.payload())
                    );
                }

                return new UpdateInfo(
                    true,
                    response.lastPushedTime(),
                    UNKNOWN.generateUpdateMessage(null)
                );
            }

            var isNewUpdate = response.lastUpdateTime().isAfter(lastUpdatedAt);

            return new UpdateInfo(
                isNewUpdate,
                response.lastUpdateTime(),
                isNewUpdate ? UNKNOWN.generateUpdateMessage(null) : "Обновлений нет"
            );

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Link url is invalid (Could not parse to URI)" + url, e);
        }
    }
}
