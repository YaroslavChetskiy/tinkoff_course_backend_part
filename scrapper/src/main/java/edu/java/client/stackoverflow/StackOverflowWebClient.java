package edu.java.client.stackoverflow;

import edu.java.dto.entity.Link;
import edu.java.dto.stackoverflow.QuestionResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import org.springframework.web.reactive.function.client.WebClient;

public class StackOverflowWebClient implements StackOverflowClient {

    private static final String DEFAULT_BASE_URL = "https://api.stackexchange.com/2.3/";

    private static final String QUESTION_ENDPOINT = "questions/{id}?site=stackoverflow";

    private final WebClient webClient;

    public StackOverflowWebClient() {
        this(DEFAULT_BASE_URL);
    }

    public StackOverflowWebClient(String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public QuestionResponse fetchQuestion(Long questionId) {
        return webClient.get()
            .uri(QUESTION_ENDPOINT, questionId)
            .retrieve()
            .bodyToMono(QuestionResponse.class)
            .block();
    }

    @Override
    public OffsetDateTime checkForUpdate(Link link) {
        try {
            var uri = new URI(link.getUrl());
            String[] pathParts = uri.getPath().split("/");
            Long questionId = Long.parseLong(pathParts[pathParts.length - 1]);

            QuestionResponse response = fetchQuestion(questionId);
            return response.items().getFirst().lastUpdateTime();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Невалидная ссылка", e);
        }
    }
}
