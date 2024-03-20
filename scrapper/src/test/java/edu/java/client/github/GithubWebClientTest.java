package edu.java.client.github;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.dto.github.EventResponse;
import edu.java.dto.github.EventResponse.Payload;
import edu.java.dto.github.EventResponse.Payload.Comment;
import edu.java.dto.github.EventResponse.Payload.Commit;
import edu.java.dto.github.EventResponse.Payload.Issue;
import edu.java.dto.github.EventResponse.Payload.PullRequest;
import edu.java.dto.github.RepositoryResponse;
import edu.java.dto.update.UpdateInfo;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static edu.java.client.github.GithubJsonResponse.EVENTS_RESPONSE_BODY;
import static edu.java.client.github.GithubJsonResponse.REPO_RESPONSE_BODY;
import static edu.java.dto.github.EventType.ISSUE_COMMENT;
import static edu.java.dto.github.EventType.PULL_REQUEST;
import static edu.java.dto.github.EventType.PUSH;
import static org.assertj.core.api.Assertions.assertThat;

class GithubWebClientTest {

    private static final String URL = "/repos/YaroslavChetskiy/tinkoff_course_backend_part";
    private static final String EVENT_URL = "/repos/YaroslavChetskiy/tinkoff_course_backend_part/events";

    private static final String OWNER = "YaroslavChetskiy";
    private static final String REPOSITORY = "tinkoff_course_backend_part";

    private static final RepositoryResponse REPO_EXPECTED_RESPONSE = new RepositoryResponse(
        "YaroslavChetskiy/tinkoff_course_backend_part",
        "https://github.com/YaroslavChetskiy/tinkoff_course_backend_part",
        OffsetDateTime.parse("2024-02-03T12:20:34Z"),
        OffsetDateTime.parse("2024-02-15T18:32:44Z")
    );

    private static final List<EventResponse> EVENTS_EXPECTED_RESPONSE = List.of(
        new EventResponse(
            PUSH,
            new Payload(
                List.of(new Commit("Fixes and refactoring after code review")),
                null,
                null,
                null
            ),
            OffsetDateTime.parse("2024-03-11T18:51:37Z")
        ),
        new EventResponse(
            ISSUE_COMMENT,
            new Payload(
                null,
                new Issue(5, "Hw5"),
                null,
                new Comment("Some body")
            ),
            OffsetDateTime.parse("2024-03-09T17:46:49Z")
        ),
        new EventResponse(
            PULL_REQUEST,
            new Payload(
                null,
                null,
                new PullRequest("Hw5"),
                null
            ),
            OffsetDateTime.parse("2024-03-09T17:46:04Z")
        )
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
                .withBody(REPO_RESPONSE_BODY))
        );

        GithubClient client = new GithubWebClient(wireMockServer.baseUrl());
        var repositoryResponse = client.fetchRepository(OWNER, REPOSITORY);

        assertThat(repositoryResponse).isEqualTo(REPO_EXPECTED_RESPONSE);
    }

    @Test
    @DisplayName("Получение информации о событиях")
    void getCorrectResponseInFetchEvents() {
        wireMockServer.stubFor(get(urlEqualTo(EVENT_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(EVENTS_RESPONSE_BODY))
        );

        GithubClient client = new GithubWebClient(wireMockServer.baseUrl());
        var eventsResponse = client.fetchEvents(OWNER, REPOSITORY);

        assertThat(eventsResponse).isEqualTo(EVENTS_EXPECTED_RESPONSE);
    }

    @Test
    @DisplayName("Парсинг ссылки и получение времени последнего обновления")
    void getCorrectParsingAndUpdateInfoInCheckForUpdate() {
        wireMockServer.stubFor(get(urlEqualTo(URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(REPO_RESPONSE_BODY))
        );

        wireMockServer.stubFor(get(urlEqualTo(EVENT_URL))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(EVENTS_RESPONSE_BODY))
        );

        GithubClient client = new GithubWebClient(wireMockServer.baseUrl());
        UpdateInfo actualResult = client.checkForUpdate(
            "https://github.com/YaroslavChetskiy/tinkoff_course_backend_part",
            REPO_EXPECTED_RESPONSE.lastPushedTime().minusDays(2)
        );

        var expectedEvent = EVENTS_EXPECTED_RESPONSE.getFirst();

        var expectedResult = new UpdateInfo(
            true,
            expectedEvent.createdAt(),
            PUSH.generateUpdateMessage(expectedEvent.payload())
        );

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @AfterAll
    static void finish() {
        wireMockServer.stop();
    }

}
