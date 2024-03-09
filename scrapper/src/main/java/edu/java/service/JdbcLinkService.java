package edu.java.service;

import edu.java.domain.repository.jdbc.JdbcChatLinkRepository;
import edu.java.domain.repository.jdbc.JdbcLinkRepository;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;

    private final JdbcChatLinkRepository chatLinkRepository;

    @Transactional(readOnly = true)
    public ListLinksResponse getAllLinks(Long chatId) {
        List<LinkResponse> links = chatLinkRepository.findAllLinksByChatId(chatId)
            .stream()
            .map(link -> new LinkResponse(link.getId(), link.getUrl()))
            .toList();
        return new ListLinksResponse(links, links.size());
    }

    @Transactional
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        Link link = linkRepository.findLinkByUrl(request.link());

        if (link != null && chatLinkRepository.isLinkTrackedInChat(chatId, link.getId())) {
            throw new LinkAlreadyTrackedException("Ссылка уже отслеживается");
        } else if (link == null) {
            linkRepository.saveLink(new Link(
                    null,
                    request.link(),
                    LinkType.resolve(request.link()),
                    null,
                    null,
                    OffsetDateTime.now()
                )
            );
        }

        Link savedLink = linkRepository.findLinkByUrl(request.link());

        chatLinkRepository.addLinkToChat(chatId, savedLink.getId());
        return new LinkResponse(savedLink.getId(), savedLink.getUrl());
    }

    @Transactional
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        Link link = linkRepository.findLinkByUrl(request.link());

        if (link == null || !chatLinkRepository.isLinkTrackedInChat(chatId, link.getId())) {
            throw new LinkNotFoundException("Вы и так не отслеживаете ссылку");
        }

        chatLinkRepository.removeLinkFromChat(chatId, link.getId());

        if (!chatLinkRepository.isLinkTracked(link.getId())) {
            linkRepository.deleteLink(link.getUrl());
        }

        return new LinkResponse(link.getId(), link.getUrl());
    }
}
