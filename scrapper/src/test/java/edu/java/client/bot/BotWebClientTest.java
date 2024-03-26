package edu.java.client.bot;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.client.github.GithubClient;
import edu.java.client.github.GithubWebClient;
import edu.java.configuration.retry.RetryConfigProperties;
import edu.java.dto.request.LinkUpdateRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static edu.java.client.github.GithubJsonResponse.REPO_RESPONSE_BODY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
class BotWebClientTest {

    private static final String URL = "/updates";

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
    @DisplayName("Отправка обновлений")
    void getCorrectResponseInSendUpdate() {
        String expectedResult = "Обновление обработано";
        var retryAbleCode = new ArrayList<>(retryConfigProperties.codes()).getFirst();
        wireMockServer.stubFor(post(urlEqualTo(URL))
            .inScenario("Retry scenario")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("Retry succeeded")
            .willReturn(aResponse()
                .withStatus(retryAbleCode)
                .withHeader("Content-Type", "text/plain")));

        wireMockServer.stubFor(post(urlEqualTo(URL))
            .inScenario("Retry scenario")
            .whenScenarioStateIs("Retry succeeded")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(expectedResult)));

        BotClient client = new BotWebClient(
            wireMockServer.baseUrl(),
            filterFunction
        );

        String actualResult = client.sendUpdate(new LinkUpdateRequest(
                1L,
                "github.com",
                "dummy",
                List.of(1L, 2L)
            )
        );

        assertThat(actualResult).isEqualTo(expectedResult);
        wireMockServer.verify(2, postRequestedFor(urlEqualTo(URL)));
    }

    @Test
    @DisplayName("Получение ошибки при достижении максимального количества попыток повторного запроса")
    void getCorrectErrorResponseAfterMaxRetry() {
        var retryAbleCode = new ArrayList<>(retryConfigProperties.codes()).getFirst();
        wireMockServer.stubFor(post(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(retryAbleCode)
                .withHeader("Content-Type", "text/plain")
            )
        );

        BotClient client = new BotWebClient(wireMockServer.baseUrl(), filterFunction);

        assertThrows(WebClientResponseException.class, () -> client.sendUpdate(
            new LinkUpdateRequest(
                1L,
                "github.com",
                "dummy",
                List.of(1L, 2L)
            )
        ));

        wireMockServer.verify(retryConfigProperties.maxAttempts() + 1, postRequestedFor((urlEqualTo(URL))));
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
