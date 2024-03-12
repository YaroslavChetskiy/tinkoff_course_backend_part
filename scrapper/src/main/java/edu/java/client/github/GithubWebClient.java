package edu.java.client.github;

import edu.java.dto.entity.Link;
import edu.java.dto.github.RepositoryResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import org.springframework.web.reactive.function.client.WebClient;

public class GithubWebClient implements GithubClient {

    private static final String DEFAULT_BASE_URL = "https://api.github.com/";

    private static final String REPOSITORY_ENDPOINT = "/repos/{owner}/{repo}";

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
    public OffsetDateTime checkForUpdate(Link link) {
        try {
            URI uri = new URI(link.getUrl());
            String[] pathParts = uri.getPath().split("/");

            RepositoryResponse response = fetchRepository(pathParts[1], pathParts[2]);

            return response.lastUpdateTime();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Link url is invalid (Could not parse to URI)" + link.getUrl(), e);
        }
    }
}
