package edu.java.service;

import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.dto.entity.Chat;
import edu.java.dto.entity.Link;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import java.time.OffsetDateTime;
import java.util.List;
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static edu.java.dto.entity.LinkType.GITHUB_REPO;
import static edu.java.dto.entity.LinkType.STACKOVERFLOW_QUESTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcLinkServiceTest {

    private static final Link LINK = new Link(
        1L,
        "https://github.com/example/repo",
        GITHUB_REPO,
        OffsetDateTime.now().minusDays(2),
        OffsetDateTime.now().minusDays(2),
        null
    );

    private static final Link SECOND_LINK = new Link(
        2L,
        "https://stackoverflow.com/questions/12345/sample-question",
        STACKOVERFLOW_QUESTION,
        OffsetDateTime.now().minusDays(2),
        OffsetDateTime.now().minusDays(2),
        null
    );

    private static final Chat CHAT = new Chat(1L, OffsetDateTime.now());

    @Mock
    private JdbcLinkRepository linkRepository;

    @Mock
    private JdbcChatLinkRepository chatLinkRepository;

    @InjectMocks
    private JdbcLinkService linkService;

    @Test
    void getAllLinks() {
        when(chatLinkRepository.findAllLinksByChatId(CHAT.getId())).thenReturn(List.of(LINK, SECOND_LINK));
        var expectedResult = new ListLinksResponse(
            List.of(
                new LinkResponse(1L, "https://github.com/example/repo"),
                new LinkResponse(2L, "https://stackoverflow.com/questions/12345/sample-question")
            ),
            2
        );

        var actualResult = linkService.getAllLinks(CHAT.getId());

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void addLink() {
        AddLinkRequest request = new AddLinkRequest(LINK.getUrl());
        when(linkRepository.findLinkByUrl(request.link())).thenReturn(LINK);
        when(chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), LINK.getId())).thenReturn(true);

        assertThrows(LinkAlreadyTrackedException.class, () -> linkService.addLink(CHAT.getId(), request));

        AddLinkRequest request2 = new AddLinkRequest(SECOND_LINK.getUrl());
        when(linkRepository.findLinkByUrl(request2.link())).thenReturn(SECOND_LINK);
        when(chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), SECOND_LINK.getId())).thenReturn(false);

        var expectedResult = new LinkResponse(SECOND_LINK.getId(), SECOND_LINK.getUrl());
        var actualResult = linkService.addLink(CHAT.getId(), request2);

        verify(chatLinkRepository, times(1)).addLinkToChat(CHAT.getId(), SECOND_LINK.getId());
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void removeLink() {
        RemoveLinkRequest request = new RemoveLinkRequest(LINK.getUrl());
        when(linkRepository.findLinkByUrl(request.link())).thenReturn(LINK);
        when(chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), LINK.getId())).thenReturn(false);

        assertThrows(LinkNotFoundException.class, () -> linkService.removeLink(CHAT.getId(), request));

        RemoveLinkRequest request2 = new RemoveLinkRequest(SECOND_LINK.getUrl());
        when(linkRepository.findLinkByUrl(request2.link())).thenReturn(SECOND_LINK);
        when(chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), SECOND_LINK.getId())).thenReturn(true);
        when(chatLinkRepository.isLinkTracked(SECOND_LINK.getId())).thenReturn(false);

        var expectedResult = new LinkResponse(SECOND_LINK.getId(), SECOND_LINK.getUrl());
        var actualResult = linkService.removeLink(CHAT.getId(), request2);

        verify(chatLinkRepository, times(1)).removeLinkFromChat(CHAT.getId(), SECOND_LINK.getId());
        verify(linkRepository, times(1)).deleteLink(SECOND_LINK.getUrl());
        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
