package edu.java.service.jpa;

import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jpa.JpaChatLinkRepository;
import edu.java.domain.repository.jpa.JpaChatRepository;
import edu.java.domain.repository.jpa.JpaLinkRepository;
import edu.java.dto.entity.hibernate.Chat;
import edu.java.dto.entity.hibernate.ChatLink;
import edu.java.dto.entity.hibernate.link.GithubRepositoryLink;
import edu.java.dto.entity.hibernate.link.Link;
import edu.java.dto.entity.hibernate.link.StackOverflowQuestionLink;
import edu.java.dto.entity.jdbc.LinkType;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import static edu.java.dto.entity.jdbc.LinkType.GITHUB_REPO;
import static edu.java.dto.entity.jdbc.LinkType.STACKOVERFLOW_QUESTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaLinkServiceTest {

    private static final OffsetDateTime DEFAULT_LAST_UPDATE_AND_CHECK_TIME = OffsetDateTime.now().minusDays(2);

    private static final String DEFAULT_GITHUB_URL = "https://github.com/dummy/dummy_repo";
    private static final String DEFAULT_STACKOVERFLOW_URL = "https://stackoverflow.com/questions/123/dummy";

    private static final Chat CHAT = Chat.builder()
        .id(255L)
        .createdAt(OffsetDateTime.now())
        .build();

    @Mock
    private JpaLinkRepository linkRepository;

    @Mock
    private JpaChatRepository chatRepository;

    @Mock
    private JpaChatLinkRepository chatLinkRepository;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @InjectMocks
    private JpaLinkService linkService;

    @ParameterizedTest
    @MethodSource("getArgumentsForGetAllLinksTest")
    void getAllLinks(int linkCount) {
        List<Link> links = IntStream.range(1, linkCount + 1).mapToObj(it -> generateLink(
                it,
                (it % 2 != 0 ? DEFAULT_GITHUB_URL : DEFAULT_STACKOVERFLOW_URL) + it,
                it % 2 != 0 ? GITHUB_REPO : STACKOVERFLOW_QUESTION
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

        when(linkRepository.findByUrl(request.link())).thenReturn(Optional.of(link));
        when(chatLinkRepository.existsByChatIdAndLinkId(CHAT.getId(), link.getId())).thenReturn(isLinkTrackedInChat);

        if (isLinkTrackedInChat) {
            assertThrows(LinkAlreadyTrackedException.class, () -> linkService.addLink(CHAT.getId(), request));
        } else {
            when(chatRepository.getReferenceById(CHAT.getId())).thenReturn(CHAT);

            var actualResult = linkService.addLink(CHAT.getId(), request);
            var expectedResult = new LinkResponse(link.getId(), request.link());

            assertThat(actualResult).isEqualTo(expectedResult);
            verify(chatLinkRepository, times(1)).save(any(ChatLink.class));
        }
    }

    static Stream<Arguments> getArgumentsForAddExistingInDatabaseLinkToChatTest() {
        return Stream.of(
            Arguments.of(generateLink(1L, DEFAULT_GITHUB_URL, GITHUB_REPO), true),
            Arguments.of(generateLink(2L, DEFAULT_STACKOVERFLOW_URL + 2, STACKOVERFLOW_QUESTION), true),
            Arguments.of(generateLink(1L, DEFAULT_GITHUB_URL, GITHUB_REPO), false),
            Arguments.of(generateLink(2L, DEFAULT_STACKOVERFLOW_URL + 2, STACKOVERFLOW_QUESTION), false)
        );
    }

    @Test
    void addNullLink() {
        var savedLink = generateLink(1L, DEFAULT_STACKOVERFLOW_URL, STACKOVERFLOW_QUESTION);

        QuestionResponse questionResponse = new QuestionResponse(
            List.of(
                new QuestionResponse.ItemResponse(
                    255L,
                    savedLink.getUrl(),
                    savedLink.getLastUpdatedAt(),
                    2
                )
            )
        );

        AddLinkRequest request = new AddLinkRequest(savedLink.getUrl());

        when(linkRepository.findByUrl(request.link())).thenReturn(Optional.empty());
        when(chatRepository.getReferenceById(CHAT.getId())).thenReturn(CHAT);
        when(linkRepository.save(any())).thenReturn(savedLink);
        when(stackOverflowClient.fetchQuestion(anyLong())).thenReturn(questionResponse);

        var actualResult = linkService.addLink(CHAT.getId(), request);
        var expectedResult = new LinkResponse(savedLink.getId(), savedLink.getUrl());

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(linkRepository, times(1)).save(any(Link.class));
        verify(chatLinkRepository, times(1)).save(any(ChatLink.class));
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForRemoveLinkTest")
    void removeLink(Link link, boolean isLinkTracked) {
        RemoveLinkRequest request = new RemoveLinkRequest(link == null ? DEFAULT_GITHUB_URL : link.getUrl());

        if (link == null) {
            when(chatLinkRepository.findChatLinkByChatIdAndLinkUrl(
                CHAT.getId(),
                request.link()
            )).thenReturn(Optional.empty());

            assertThrows(LinkNotFoundException.class, () -> linkService.removeLink(CHAT.getId(), request));
        } else {
            ChatLink chatLink = ChatLink.builder()
                .chat(CHAT)
                .link(link)
                .build();

            when(chatLinkRepository.findChatLinkByChatIdAndLinkUrl(
                CHAT.getId(),
                request.link()
            )).thenReturn(Optional.of(chatLink));

            var expectedResult = new LinkResponse(link.getId(), link.getUrl());
            var actualResult = linkService.removeLink(CHAT.getId(), request);

            verify(chatLinkRepository, times(1)).delete(chatLink);

            if (!isLinkTracked) {
                verify(linkRepository, times(1)).delete(link);
            }

            assertThat(actualResult).isEqualTo(expectedResult);
        }
    }

    static Stream<Arguments> getArgumentsForRemoveLinkTest() {
        return Stream.of(
            Arguments.of(generateLink(1L, DEFAULT_GITHUB_URL, GITHUB_REPO), false),
            Arguments.of(generateLink(2L, DEFAULT_STACKOVERFLOW_URL + 2, STACKOVERFLOW_QUESTION), true),
            Arguments.of(null, true),
            Arguments.of(null, false)
        );
    }

    private static Link generateLink(long id, String url, LinkType type) {
        if (type == GITHUB_REPO) {
            return GithubRepositoryLink.builder()
                .id(id)
                .url(url)
                .chatLinks(new ArrayList<>())
                .lastUpdatedAt(DEFAULT_LAST_UPDATE_AND_CHECK_TIME)
                .checkedAt(DEFAULT_LAST_UPDATE_AND_CHECK_TIME)
                .createdAt(OffsetDateTime.now())
                .build();
        }
        return StackOverflowQuestionLink.builder()
            .id(id)
            .url(url)
            .chatLinks(new ArrayList<>())
            .lastUpdatedAt(DEFAULT_LAST_UPDATE_AND_CHECK_TIME)
            .checkedAt(DEFAULT_LAST_UPDATE_AND_CHECK_TIME)
            .createdAt(OffsetDateTime.now())
            .build();
    }
}
