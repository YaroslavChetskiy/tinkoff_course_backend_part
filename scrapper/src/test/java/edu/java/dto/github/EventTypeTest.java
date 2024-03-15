package edu.java.dto.github;

import edu.java.dto.github.EventResponse.Payload;
import edu.java.dto.github.EventResponse.Payload.Commit;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static edu.java.dto.github.EventType.ISSUE_COMMENT;
import static edu.java.dto.github.EventType.PULL_REQUEST;
import static edu.java.dto.github.EventType.PUSH;
import static edu.java.dto.github.EventType.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

class EventTypeTest {

    @ParameterizedTest
    @DisplayName("Проверка генерации сообщений об обновлении")
    @MethodSource("getArgumentsForGenerateUpdateMessageTest")
    void generateUpdateMessage(EventType type, Payload payload, String expectedResult) {
        var actualResult = type.generateUpdateMessage(payload);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getArgumentsForGenerateUpdateMessageTest() {
        return Stream.of(
            Arguments.of(
                PUSH,
                new Payload(
                    List.of(new Commit("Fixes and refactoring after code review")),
                    null,
                    null,
                    null
                ),
                """
                    Обновление: произошёл push в репозиторий
                    Подробности:
                    Commits:
                    \tCommit 1: Fixes and refactoring after code review
                    """
            ),
            Arguments.of(
                ISSUE_COMMENT,
                new Payload(
                    null,
                    new Payload.Issue(5, "Hw5"),
                    null,
                    new Payload.Comment("Some body")
                ),
                """
                    Обновление: Issue[5][Hw5]: Добавлен новый комментарий:
                    Some body
                    """
            ),
            Arguments.of(
                PULL_REQUEST,
                new Payload(
                    null,
                    null,
                    new Payload.PullRequest("Hw5"),
                    null
                ),
                "Обновление: создан новый pull request[Hw5]"
            ),
            Arguments.of(
                UNKNOWN,
                new Payload(
                    null,
                    null,
                    null,
                    null
                ),
                "В репозитории произошло обновление"
            )
        );
    }
}
