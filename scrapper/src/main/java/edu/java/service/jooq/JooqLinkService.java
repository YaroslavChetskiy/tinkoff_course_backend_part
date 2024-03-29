package edu.java.service.jooq;

import edu.java.client.stackoverflow.StackOverflowClient;
import edu.java.domain.repository.jooq.JooqChatLinkRepository;
import edu.java.domain.repository.jooq.JooqLinkRepository;
import edu.java.domain.repository.jooq.JooqQuestionRepository;
import edu.java.dto.entity.jdbc.Link;
import edu.java.dto.entity.jdbc.LinkType;
import edu.java.dto.entity.jdbc.Question;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import static edu.java.dto.entity.jdbc.LinkType.STACKOVERFLOW_QUESTION;

// честно не знаю, что может поменяться, кроме как реализации chatRepository,
// но раз в задании говорят, что нужно делать разные реализации сервисов, меняя
// реализацию репозиториев, то сделаю
@RequiredArgsConstructor
public class JooqLinkService implements LinkService {

    private final JooqLinkRepository linkRepository;
    private final JooqChatLinkRepository chatLinkRepository;
    private final JooqQuestionRepository questionRepository;
    private final StackOverflowClient stackOverflowWebClient;

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
        }

        Link savedLink = link;

        if (link == null) {
            savedLink = linkRepository.saveLink(new Link(
                    null,
                    request.link(),
                    LinkType.resolve(request.link()),
                    null,
                    null,
                    OffsetDateTime.now()
                )
            );

            if (savedLink.getType() == STACKOVERFLOW_QUESTION) {
                var question = stackOverflowWebClient
                    .fetchQuestion(stackOverflowWebClient.getQuestionId(savedLink.getUrl()))
                    .items()
                    .getFirst();
                questionRepository.saveQuestion(new Question(null, question.answerCount(), savedLink.getId()));
            }
        }

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
