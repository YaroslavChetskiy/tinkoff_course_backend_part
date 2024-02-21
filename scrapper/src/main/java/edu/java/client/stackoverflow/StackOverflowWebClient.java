package edu.java.client.stackoverflow;

import edu.java.dto.stackoverflow.QuestionResponse;
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
}
