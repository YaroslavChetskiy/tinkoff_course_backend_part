package edu.java.client.bot;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.dto.request.LinkUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class BotWebClientTest {

    private static final String URL = "/updates";

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void prepare() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @Test
    @DisplayName("Отправка обновлений")
    void getCorrectResponseInSendUpdate() {
        String expectedResult = "Обновление обработано";

        wireMockServer.stubFor(post(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(expectedResult))
        );

        BotClient client = new BotWebClient(wireMockServer.baseUrl());
        String actualResult = client.sendUpdate(new LinkUpdateRequest(
                1L,
                "github.com",
                "dummy",
                List.of(1L, 2L)
            )
        );

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
