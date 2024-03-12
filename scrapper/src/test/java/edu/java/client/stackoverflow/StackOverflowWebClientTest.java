package edu.java.client.stackoverflow;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.dto.stackoverflow.QuestionResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class StackOverflowWebClientTest {

    private static final String URL = "/questions/25630159?site=stackoverflow";

    private static final String RESPONSE_BODY = """
        {
          "items": [
            {
              "tags": [
                "c#"
              ],
              "owner": {
                "account_id": 3078547,
                "reputation": 465,
                "user_id": 2607332,
                "user_type": "registered",
                "accept_rate": 50,
                "profile_image": "https://graph.facebook.com/517527802/picture?type=large",
                "display_name": "Simon Price",
                "link": "https://stackoverflow.com/users/2607332/simon-price"
              },
              "is_answered": true,
              "view_count": 474,
              "closed_date": 1409687590,
              "accepted_answer_id": 25630325,
              "answer_count": 2,
              "score": 0,
              "last_activity_date": 1519360722,
              "creation_date": 1409683372,
              "last_edit_date": 1519360722,
              "question_id": 25630159,
              "link": "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
              "closed_reason": "Not suitable for this site",
              "title": "Connect to Stack Overflow API"
            }
          ],
          "has_more": false,
          "quota_max": 10000,
          "quota_remaining": 9957
        }
        """;

    private static final Long ID = 25630159L;

    private static final QuestionResponse EXPECTED_RESPONSE = new QuestionResponse(
        List.of(new QuestionResponse.ItemResponse(
            ID,
            "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
            OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(1519360722),
                ZoneOffset.UTC
            )
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

    @Test
    @DisplayName("Парсинг ссылки и получение времени последнего обновления")
    void getCorrectParsingAndLastUpdateTimeInCheckForUpdate() {
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(RESPONSE_BODY))
        );

        StackOverflowClient client = new StackOverflowWebClient(wireMockServer.baseUrl());
        OffsetDateTime actualResult = client.checkForUpdate(new Link(
                1L,
                "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
                LinkType.STACKOVERFLOW_QUESTION,
                null,
                null,
                null
            )
        );

        assertThat(actualResult).isEqualTo(EXPECTED_RESPONSE.items().getFirst().lastUpdateTime());
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
