package edu.java.client.stackoverflow;

import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.stackoverflow.QuestionResponse.ItemResponse;
import edu.java.dto.update.UpdateInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

public class StackOverflowWebClient implements StackOverflowClient {

    private static final String DEFAULT_BASE_URL = "https://api.stackexchange.com/2.3/";

    private static final String QUESTION_ENDPOINT = "questions/{id}?site=stackoverflow";
    private static final int DEFAULT_PARTS_LENGTH = 4;

    private final WebClient webClient;

    public StackOverflowWebClient(ExchangeFilterFunction filterFunction) {
        this(DEFAULT_BASE_URL, filterFunction);
    }

    public StackOverflowWebClient(String baseUrl, ExchangeFilterFunction filterFunction) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .filter(filterFunction)
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
    public UpdateInfo checkForUpdate(String url, OffsetDateTime lastUpdatedAt, int answerCount, ItemResponse question) {
        if (question.lastUpdateTime().isAfter(lastUpdatedAt)) {
            if (question.answerCount() > answerCount) {
                return new UpdateInfo(true, question.lastUpdateTime(), "Появился новый ответ");
            }
            return new UpdateInfo(true, question.lastUpdateTime(), "Произошло обновление в вопросе");
        }
        return new UpdateInfo(false, question.lastUpdateTime(), "Обновлений нет");

    }

    @Override
    public Long getQuestionId(String url) {
        try {
            var uri = new URI(url);
            String[] pathParts = uri.getPath().split("/");
            return Long.parseLong(pathParts[pathParts.length == DEFAULT_PARTS_LENGTH
                ? (pathParts.length - 2)
                : (pathParts.length) - 1]);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Link url is invalid (Could not parse to URI)" + url, e);
        }
    }

}
