package edu.java.client.stackoverflow;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.update.UpdateInfo;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static edu.java.client.stackoverflow.StackOverflowJsonResponse.RESPONSE_BODY;
import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeAll
    static void prepare() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
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

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl());
        var repositoryResponse = client.fetchQuestion(ID);

        assertThat(repositoryResponse).isEqualTo(EXPECTED_RESPONSE);
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

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl());

        var expectedQuestion = EXPECTED_RESPONSE.items().getFirst();

        UpdateInfo actualResult = client.checkForUpdate(
            new Link(
                1L,
                "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
                LinkType.STACKOVERFLOW_QUESTION,
                null,
                lastUpdatedAt,
                null
            ),
            answerCount
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
        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl());

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
            )
        );
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
