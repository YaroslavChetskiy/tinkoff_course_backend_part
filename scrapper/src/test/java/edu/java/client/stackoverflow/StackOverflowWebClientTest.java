package edu.java.client.stackoverflow;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.configuration.retry.RetryConfigProperties;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.update.UpdateInfo;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static edu.java.client.stackoverflow.StackOverflowJsonResponse.RESPONSE_BODY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
class StackOverflowWebClientTest {

    private static final String URL = "/questions/25630159?site=stackoverflow";

    private static final Long ID = 25630159L;

    private static final QuestionResponse EXPECTED_RESPONSE = new QuestionResponse(
        List.of(new QuestionResponse.ItemResponse(
            ID,
            "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
            OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(1519360722),
                ZoneOffset.UTC
            ),
            2
        ))
    );

    private static WireMockServer wireMockServer;

    @Autowired
    private RetryConfigProperties retryConfigProperties;

    @Autowired
    private ExchangeFilterFunction filterFunction;

    @BeforeAll
    static void prepare() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @BeforeEach
    void reset() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Получение информации о репозитории")
    void getCorrectResponseInFetchQuestion() {
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(RESPONSE_BODY))
        );

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl(), filterFunction);
        var repositoryResponse = client.fetchQuestion(ID);

        assertThat(repositoryResponse).isEqualTo(EXPECTED_RESPONSE);
    }

    @Test
    @DisplayName("Получение информации о репозитории с механизмом повторного запроса")
    void getCorrectResponseInFetchRepositoryWithRetry() {
        var retryAbleCode = new ArrayList<>(retryConfigProperties.codes()).getFirst();
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .inScenario("Retry scenario")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("Retry succeeded")
            .willReturn(aResponse()
                .withStatus(retryAbleCode)
                .withHeader("Content-Type", "application/json")
            )
        );

        wireMockServer.stubFor(get(urlEqualTo(URL))
            .inScenario("Retry scenario")
            .whenScenarioStateIs("Retry succeeded")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(RESPONSE_BODY)
            )
        );

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl(), filterFunction);
        var repositoryResponse = client.fetchQuestion(ID);

        assertThat(repositoryResponse).isEqualTo(EXPECTED_RESPONSE);
        wireMockServer.verify(2, getRequestedFor((urlEqualTo(URL))));
    }

    @Test
    @DisplayName("Получение ошибки при достижении максимального количества попыток повторного запроса")
    void getCorrectErrorResponseAfterMaxRetry() {
        var retryAbleCode = new ArrayList<>(retryConfigProperties.codes()).getFirst();
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(retryAbleCode)
                .withHeader("Content-Type", "application/json"))
        );

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl(), filterFunction);

        assertThrows(WebClientResponseException.class, () -> client.fetchQuestion(ID));

        wireMockServer.verify(retryConfigProperties.maxAttempts() + 1, getRequestedFor((urlEqualTo(URL))));
    }

    @ParameterizedTest
    @DisplayName("Получение корректной информации об обновлении")
    @MethodSource("getArgumentsForCheckForUpdateTest")
    void getCorrectUpdateInfoInCheckForUpdate(
        OffsetDateTime lastUpdatedAt,
        int answerCount,
        boolean isNewUpdate,
        String message
    ) {
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(RESPONSE_BODY))
        );

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl(), filterFunction);

        var expectedQuestion = EXPECTED_RESPONSE.items().getFirst();

        var questionResponse = client.fetchQuestion(ID).items().getFirst();

        UpdateInfo actualResult = client.checkForUpdate(
            "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
            lastUpdatedAt,
            answerCount,
            questionResponse
        );

        assertThat(actualResult).isEqualTo(new UpdateInfo(
            isNewUpdate,
            expectedQuestion.lastUpdateTime(),
            message
        ));
    }

    static Stream<Arguments> getArgumentsForCheckForUpdateTest() {
        return Stream.of(
            Arguments.of(
                EXPECTED_RESPONSE.items().getFirst().lastUpdateTime(),
                2,
                false,
                "Обновлений нет"
            ),
            Arguments.of(
                EXPECTED_RESPONSE.items().getFirst().lastUpdateTime().minusDays(1L),
                2,
                true,
                "Произошло обновление в вопросе"
            ),
            Arguments.of(
                EXPECTED_RESPONSE.items().getFirst().lastUpdateTime().minusDays(1L),
                1,
                true,
                "Появился новый ответ"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForGetQuestionIdTest")
    void getQuestionId(String url, long expectedResult) {
        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl(), filterFunction);

        var actualResult = client.getQuestionId(url);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getArgumentsForGetQuestionIdTest() {
        return Stream.of(
            Arguments.of(
                "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
                25630159L
            ),
            Arguments.of(
                "https://stackoverflow.com/questions/123/dummy",
                123L
            ),
            Arguments.of(
                "https://stackoverflow.com/questions/1/another_dummy",
                1L
            ),
            Arguments.of(
                "https://stackoverflow.com/questions/123/",
                123L
            ),
            Arguments.of(
                "https://stackoverflow.com/questions/123",
                123L
            )
        );
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
