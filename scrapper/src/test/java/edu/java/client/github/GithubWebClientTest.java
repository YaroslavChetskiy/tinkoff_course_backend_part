package edu.java.client.github;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.dto.github.RepositoryResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class GithubWebClientTest {

    private static final String URL = "/repos/YaroslavChetskiy/tinkoff_course_backend_part";

    private static final String RESPONSE_BODY = """
        {
          "id": 752229016,
          "node_id": "R_kgDOLNYamA",
          "name": "tinkoff_course_backend_part",
          "full_name": "YaroslavChetskiy/tinkoff_course_backend_part",
          "html_url": "https://github.com/YaroslavChetskiy/tinkoff_course_backend_part",
          "created_at": "2024-02-03T12:17:54Z",
          "updated_at": "2024-02-03T12:20:34Z",
          "pushed_at": "2024-02-15T18:32:44Z"
        }
        """;

    private static final String OWNER = "YaroslavChetskiy";
    private static final String REPOSITORY = "tinkoff_course_backend_part";

    private static final RepositoryResponse EXPECTED_RESPONSE = new RepositoryResponse(
        "YaroslavChetskiy/tinkoff_course_backend_part",
        "https://github.com/YaroslavChetskiy/tinkoff_course_backend_part",
        OffsetDateTime.parse("2024-02-03T12:20:34Z")
    );

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void prepare() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @Test
    @DisplayName("Получение информации о репозитории")
    void getCorrectResponseInFetchRepository() {
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(RESPONSE_BODY))
        );

        GithubClient client = new GithubWebClient(wireMockServer.baseUrl());
        var repositoryResponse = client.fetchRepository(OWNER, REPOSITORY);

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

        GithubClient client = new GithubWebClient(wireMockServer.baseUrl());
        OffsetDateTime actualResult = client.checkForUpdate(new Link(
                1L,
                "https://github.com/YaroslavChetskiy/tinkoff_course_backend_part",
                LinkType.GITHUB_REPO,
                null,
                null,
                null
            )
        );

        assertThat(actualResult).isEqualTo(EXPECTED_RESPONSE.lastUpdateTime());
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
