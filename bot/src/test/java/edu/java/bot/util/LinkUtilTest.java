package edu.java.bot.util;

import edu.java.bot.model.entity.Link;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static edu.java.bot.util.LinkUtil.parse;
import static edu.java.bot.util.LinkUtil.supports;
import static org.assertj.core.api.Assertions.assertThat;

class LinkUtilTest {

    @ParameterizedTest
    @DisplayName("Парсинг ссылки")
    @MethodSource("getArgumentsForGetParsedLinkFromURITest")
    void getParsedLinkFromURI(URI url, Link expectedResult) {
        var actualResult = parse(url);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getArgumentsForGetParsedLinkFromURITest() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                new URI("https://github.com/sanyarnd/tinkoff-java-course-2023/"),
                new Link(
                    "https",
                    "github.com",
                    "/sanyarnd/tinkoff-java-course-2023/",
                    null,
                    null
                )
            ),
            Arguments.of(
                new URI("https://www.stackoverflow.com/search?q=unsupported"),
                new Link(
                    "https",
                    "www.stackoverflow.com",
                    "/search",
                    "q=unsupported",
                    null
                )
            ),
            Arguments.of(
                new URI("https://www.stackoverflow.com/search?q=unsupported#anchor1"),
                new Link(
                    "https",
                    "www.stackoverflow.com",
                    "/search",
                    "q=unsupported",
                    "anchor1"
                )
            )
        );
    }

    @ParameterizedTest
    @DisplayName("Проверка поддержки ресурса")
    @MethodSource("getArgumentsForCorrectSupportsCapabilityTest")
    void getCorrectSupportsCapability(URI url, boolean expectedResult) {
        var actualResult = supports(url);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getArgumentsForCorrectSupportsCapabilityTest() throws URISyntaxException {
        return Stream.of(
            Arguments.of(
                new URI("https://github.com/sanyarnd/tinkoff-java-course-2023/"),
                true
            ),
            Arguments.of(
                new URI("https://www.stackoverflow.com/search?q=unsupported"),
                false
            ),
            Arguments.of(
                new URI("https://stackoverflow.com/questions/123/dummy"),
                true
            ),
            Arguments.of(
                new URI("https://stackoverflow.com/questions/123"),
                true
            ),
            Arguments.of(
                new URI("github.com"),
                false
            ),
            Arguments.of(
                new URI("https://dummy.com/catalog/"),
                false
            )
        );
    }
}
