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
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatRepository chatRepository;
    private final JpaChatLinkRepository chatLinkRepository;
    private final StackOverflowClient stackOverflowClient;

    @Override
    public ListLinksResponse getAllLinks(Long chatId) {
        List<LinkResponse> links = chatLinkRepository.findAllLinksByChatId(chatId)
            .stream()
            .map(link -> new LinkResponse(link.getId(), link.getUrl()))
            .toList();
        return new ListLinksResponse(links, links.size());
    }

    @Override
    @Transactional
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        Optional<Link> link = linkRepository.findByUrl(request.link());

        link.ifPresent(l -> {
            if (chatLinkRepository.existsByChatIdAndLinkId(chatId, l.getId())) {
                throw new LinkAlreadyTrackedException("Ссылка уже отслеживается");
            }
        });

        Chat chat = chatRepository.getReferenceById(chatId);
        Link savedLink = link.orElseGet(() -> {
            if (LinkType.resolve(request.link()) == LinkType.GITHUB_REPO) {
                return linkRepository.save(GithubRepositoryLink.builder()
                    .url(request.link())
                    .checkedAt(OffsetDateTime.now())
                    .lastUpdatedAt(OffsetDateTime.now())
                    .chatLinks(new ArrayList<>())
                    .build()
                );
            } else {
                var question = stackOverflowClient
                    .fetchQuestion(stackOverflowClient.getQuestionId(request.link()))
                    .items()
                    .getFirst();
                return linkRepository.save(StackOverflowQuestionLink.builder()
                    .url(request.link())
                    .checkedAt(OffsetDateTime.now())
                    .lastUpdatedAt(OffsetDateTime.now())
                    .chatLinks(new ArrayList<>())
                    .answerCount(question.answerCount())
                    .build()
                );
            }
        });

        ChatLink chatLink = new ChatLink();
        chatLink.setChat(chat);
        chatLink.setLink(savedLink);

        chatLinkRepository.save(chatLink);
        return new LinkResponse(savedLink.getId(), savedLink.getUrl());
    }

    @Override
    @Transactional
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        Optional<ChatLink> chatLinkOptional = chatLinkRepository.findChatLinkByChatIdAndLinkUrl(chatId, request.link());

        if (chatLinkOptional.isEmpty()) {
            throw new LinkNotFoundException("Вы и так не отслеживаете ссылку");
        }

        ChatLink chatLink = chatLinkOptional.get();

        chatLinkRepository.delete(chatLink);

        var link = chatLink.getLink();

        if (!chatLinkRepository.existsByLinkId(link.getId())) {
            linkRepository.delete(link);
        }

        return new LinkResponse(link.getId(), link.getUrl());
    }
}
