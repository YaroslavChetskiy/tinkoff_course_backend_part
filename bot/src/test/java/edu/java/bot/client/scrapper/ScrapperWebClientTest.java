package edu.java.bot.client.scrapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.bot.configuration.retry.RetryConfigProperties;
import edu.java.bot.model.dto.request.AddLinkRequest;
import edu.java.bot.model.dto.request.RemoveLinkRequest;
import edu.java.bot.model.dto.response.LinkResponse;
import edu.java.bot.model.dto.response.ListLinksResponse;
import java.util.ArrayList;
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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
class ScrapperWebClientTest {

    private static final Long CHAT_ID = 1L;

    private static final String CHAT_URL = "/tg-chat/1";

    private static final String LINKS_URL = "/links";

    private static final LinkResponse FIRST_LINK_RESPONSE = new LinkResponse(1L, "dummy.com");
    private static final LinkResponse SECOND_LINK_RESPONSE = new LinkResponse(2L, "dummy2.com");

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
    @DisplayName("Регистрация чата")
    void getCorrectResponseInRegisterChat() {
        String expectedResult = "Чат зарегистрирован";

        wireMockServer.stubFor(post(urlEqualTo(CHAT_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(expectedResult))
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);

        String actualResult = client.registerChat(CHAT_ID);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Получение информации о репозитории с механизмом повторного запроса")
    void getCorrectResponseInFetchRepositoryWithRetry() {
        String expectedResult = "Чат зарегистрирован";
        var retryAbleCode = new ArrayList<>(retryConfigProperties.codes()).getFirst();
        wireMockServer.stubFor(post(urlEqualTo(CHAT_URL))
            .inScenario("Retry scenario")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("Retry succeeded")
            .willReturn(aResponse()
                .withStatus(retryAbleCode)
                .withHeader("Content-Type", "text/plain")
            )
        );

        wireMockServer.stubFor(post(urlEqualTo(CHAT_URL))
            .inScenario("Retry scenario")
            .whenScenarioStateIs("Retry succeeded")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(expectedResult)
            )
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);
        var repositoryResponse = client.registerChat(CHAT_ID);

        assertThat(repositoryResponse).isEqualTo(expectedResult);
        wireMockServer.verify(2, postRequestedFor((urlEqualTo(CHAT_URL))));
    }

    @Test
    @DisplayName("Получение ошибки при достижении максимального количества попыток повторного запроса")
    void getCorrectErrorResponseAfterMaxRetry() {
        var retryAbleCode = new ArrayList<>(retryConfigProperties.codes()).getFirst();
        wireMockServer.stubFor(post(urlEqualTo(CHAT_URL))
            .willReturn(aResponse()
                .withStatus(retryAbleCode)
                .withHeader("Content-Type", "text/plain"))
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);

        assertThrows(WebClientResponseException.class, () -> client.registerChat(CHAT_ID));

        wireMockServer.verify(retryConfigProperties.maxAttempts() + 1, postRequestedFor((urlEqualTo(CHAT_URL))));
    }

    @Test
    @DisplayName("Удаление чата")
    void getCorrectResponseInDeleteChat() {
        String expectedResult = "Чат успешно удалён";

        wireMockServer.stubFor(delete(urlEqualTo(CHAT_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(expectedResult))
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);

        String actualResult = client.deleteChat(CHAT_ID);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Получение списка ссылок")
    void getCorrectResponseInGetAllLinks() {
        ListLinksResponse expectedResult = new ListLinksResponse(
            List.of(FIRST_LINK_RESPONSE, SECOND_LINK_RESPONSE),
            2
        );

        wireMockServer.stubFor(get(urlEqualTo(LINKS_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "links": [
                        {
                          "id": 1,
                          "url": "dummy.com"
                        },
                        {
                          "id": 2,
                          "url": "dummy2.com"
                        }
                      ],
                      "size": 2
                    }
                    """))
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);

        ListLinksResponse actualResult = client.getAllLinks(CHAT_ID);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Добавление ссылки")
    void getCorrectResponseInAddLink() {

        var request = new AddLinkRequest("dummy.com");

        wireMockServer.stubFor(post(urlEqualTo(LINKS_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "id": 1,
                      "url": "dummy.com"
                    }
                    """))
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);

        LinkResponse actualResult = client.addLink(CHAT_ID, request);
        assertThat(actualResult).isEqualTo(FIRST_LINK_RESPONSE);
    }

    @Test
    @DisplayName("Удаление ссылки")
    void getCorrectResponseInRemoveLink() {

        var request = new RemoveLinkRequest("dummy.com");

        wireMockServer.stubFor(delete(urlEqualTo(LINKS_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "id": 1,
                      "url": "dummy.com"
                    }
                    """))
        );

        ScrapperClient client = new ScrapperWebClient(wireMockServer.baseUrl(), filterFunction);

        LinkResponse actualResult = client.removeLink(CHAT_ID, request);
        assertThat(actualResult).isEqualTo(FIRST_LINK_RESPONSE);
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
