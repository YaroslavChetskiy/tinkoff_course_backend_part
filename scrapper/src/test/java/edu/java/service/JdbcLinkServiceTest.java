package edu.java.service;

import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.dto.entity.Chat;
import edu.java.dto.entity.Link;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static edu.java.dto.entity.LinkType.GITHUB_REPO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcLinkServiceTest {

    private static final OffsetDateTime DEFAULT_LAST_UPDATE_AND_CHECK_TIME = OffsetDateTime.now().minusDays(2);

    private static final String DEFAULT_URL = "https://github.com/dummy/dummy_repo";
    private static final Chat CHAT = new Chat(1L, OffsetDateTime.now());

    @Mock
    private JdbcLinkRepository linkRepository;

    @Mock
    private JdbcChatLinkRepository chatLinkRepository;

    @InjectMocks
    private JdbcLinkService linkService;

    @ParameterizedTest
    @MethodSource("getArgumentsForGetAllLinksTest")
    void getAllLinks(int linkCount) {
        List<Link> links = IntStream.range(1, linkCount + 1).mapToObj(it -> generateLink(
                it,
                DEFAULT_URL + it
            )
        ).toList();

        when(chatLinkRepository.findAllLinksByChatId(CHAT.getId())).thenReturn(links);

        var expectedResult = new ListLinksResponse(
            links.stream().map(link -> new LinkResponse(link.getId(), link.getUrl())).toList(),
            linkCount
        );

        var actualResult = linkService.getAllLinks(CHAT.getId());

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getArgumentsForGetAllLinksTest() {
        return Stream.of(
            Arguments.of(1),
            Arguments.of(2),
            Arguments.of(3),
            Arguments.of(4)
        );
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForAddExistingInDatabaseLinkToChatTest")
    void addExistingInDatabaseLinkToChat(Link link, boolean isLinkTrackedInChat) {
        AddLinkRequest request = new AddLinkRequest(link.getUrl());

        when(linkRepository.findLinkByUrl(request.link())).thenReturn(link);
        when(chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), link.getId())).thenReturn(isLinkTrackedInChat);

        if (isLinkTrackedInChat) {
            assertThrows(LinkAlreadyTrackedException.class, () -> linkService.addLink(CHAT.getId(), request));
        } else {
            var actualResult = linkService.addLink(CHAT.getId(), request);
            var expectedResult = new LinkResponse(link.getId(), request.link());
            assertThat(actualResult).isEqualTo(expectedResult);
            verify(chatLinkRepository, times(1)).addLinkToChat(CHAT.getId(), link.getId());
        }
    }

    static Stream<Arguments> getArgumentsForAddExistingInDatabaseLinkToChatTest() {
        return Stream.of(
            Arguments.of(generateLink(1L, DEFAULT_URL), true),
            Arguments.of(generateLink(2L, DEFAULT_URL + 2), true),
            Arguments.of(generateLink(1L, DEFAULT_URL), false),
            Arguments.of(generateLink(2L, DEFAULT_URL + 2), false)
        );
    }

    @Test
    void addNullLink() {
        AddLinkRequest request = new AddLinkRequest(DEFAULT_URL);

        var savedLink = generateLink(1L, DEFAULT_URL);

        when(linkRepository.findLinkByUrl(request.link())).thenReturn(null);
        when(linkRepository.saveLink(any())).thenReturn(savedLink);

        var actualResult = linkService.addLink(CHAT.getId(), request);
        var expectedResult = new LinkResponse(savedLink.getId(), savedLink.getUrl());

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(linkRepository, times(1)).saveLink(any());
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForRemoveLinkTest")
    void removeLink(Link link, boolean isLinkTrackedInChat, boolean isLinkTracked) {
        RemoveLinkRequest request = new RemoveLinkRequest(link == null ? DEFAULT_URL : link.getUrl());
        when(linkRepository.findLinkByUrl(request.link())).thenReturn(link);
        if (link != null) {
            when(chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), link.getId())).thenReturn(isLinkTrackedInChat);
        }
        if (link == null || !isLinkTrackedInChat) {
            assertThrows(LinkNotFoundException.class, () -> linkService.removeLink(CHAT.getId(), request));
        } else {
            when(chatLinkRepository.isLinkTracked(link.getId())).thenReturn(isLinkTracked);

            var expectedResult = new LinkResponse(link.getId(), link.getUrl());
            var actualResult = linkService.removeLink(CHAT.getId(), request);

            verify(chatLinkRepository, times(1)).removeLinkFromChat(CHAT.getId(), link.getId());

            if (!isLinkTracked) {
                verify(linkRepository, times(1)).deleteLink(link.getUrl());
            }

            assertThat(actualResult).isEqualTo(expectedResult);
        }
    }

    static Stream<Arguments> getArgumentsForRemoveLinkTest() {
        return Stream.of(
            Arguments.of(generateLink(1L, DEFAULT_URL), true, false),
            Arguments.of(generateLink(2L, DEFAULT_URL + 2), true, true),
            Arguments.of(generateLink(3L, DEFAULT_URL + 3), false, true),
            Arguments.of(null, false, false)
        );
    }

    private static Link generateLink(long id, String url) {
        return new Link(
            id,
            url,
            GITHUB_REPO,
            DEFAULT_LAST_UPDATE_AND_CHECK_TIME,
            DEFAULT_LAST_UPDATE_AND_CHECK_TIME,
            OffsetDateTime.now()
        );
    }
}
