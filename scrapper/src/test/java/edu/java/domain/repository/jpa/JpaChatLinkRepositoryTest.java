package edu.java.domain.repository.jpa;

import edu.java.dto.entity.hibernate.Chat;
import edu.java.dto.entity.hibernate.ChatLink;
import edu.java.dto.entity.hibernate.link.GithubRepositoryLink;
import edu.java.dto.entity.hibernate.link.Link;
import edu.java.dto.entity.hibernate.link.StackOverflowQuestionLink;
import edu.java.scrapper.IntegrationTest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class JpaChatLinkRepositoryTest extends IntegrationTest {

    private static final Link GITHUB_LINK = GithubRepositoryLink.builder()
        .id(1L)
        .url("github.com/dummy/dummy_repo")
        .chatLinks(new ArrayList<>())
        .build();

    private static final Link STACKOVERFLOW_LINK = StackOverflowQuestionLink.builder()
        .id(2L)
        .url("https://stackoverflow.com/questions/123/dummy")
        .chatLinks(new ArrayList<>())
        .build();

    private static final Chat CHAT = Chat.builder()
        .id(255L)
        .createdAt(OffsetDateTime.now())
        .build();

    private static final Chat SECOND_CHAT = Chat.builder()
        .id(100L)
        .createdAt(OffsetDateTime.now())
        .build();

    @Autowired
    private JpaChatLinkRepository chatLinkRepository;

    @Autowired
    private JpaChatRepository chatRepository;

    @Autowired
    private JpaLinkRepository linkRepository;

    @Test
    @Transactional
    @Rollback
    void save() {
        var savedLink = linkRepository.save(GITHUB_LINK);
        var savedChat = chatRepository.save(CHAT);

        ChatLink chatLink = new ChatLink();
        chatLink.setChat(savedChat);
        chatLink.setLink(savedLink);

        var savedChatLink = chatLinkRepository.save(chatLink);

        var chatLinks = chatLinkRepository.findAll();

        assertThat(chatLinks).hasSize(1);
        assertThat(chatLinks.getFirst()).isEqualTo(savedChatLink);
    }

    @Test
    @Transactional
    @Rollback
    void delete() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedStackOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);
        var savedChat = chatRepository.save(CHAT);

        ChatLink chatLink1 = new ChatLink();
        chatLink1.setChat(savedChat);
        chatLink1.setLink(savedGithubLink);
        var savedChatLink1 = chatLinkRepository.save(chatLink1);

        ChatLink chatLink2 = new ChatLink();
        chatLink1.setChat(savedChat);
        chatLink1.setLink(savedStackOverflowLink);
        var savedChatLink2 = chatLinkRepository.save(chatLink2);

        chatLinkRepository.delete(savedChatLink1);

        var chatLinks = chatLinkRepository.findAll();

        assertThat(chatLinks).hasSize(1);
        assertThat(chatLinks.getFirst()).isEqualTo(savedChatLink2);
    }

    @Test
    @Transactional
    @Rollback
    void existsByChatIdAndLinkId() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedChat1 = chatRepository.save(CHAT);
        var savedChat2 = chatRepository.save(SECOND_CHAT);

        ChatLink chatLink = new ChatLink();
        chatLink.setChat(savedChat1);
        chatLink.setLink(savedGithubLink);

        chatLinkRepository.save(chatLink);

        boolean actualResult1 = chatLinkRepository.existsByChatIdAndLinkId(savedChat1.getId(), savedGithubLink.getId());

        assertThat(actualResult1).isTrue();

        boolean actualResult2 = chatLinkRepository.existsByChatIdAndLinkId(savedChat2.getId(), savedGithubLink.getId());

        assertThat(actualResult2).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void existsByLinkId() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);
        var savedChat = chatRepository.save(CHAT);

        ChatLink chatLink = new ChatLink();
        chatLink.setChat(savedChat);
        chatLink.setLink(savedGithubLink);

        chatLinkRepository.save(chatLink);

        boolean actualResult1 = chatLinkRepository.existsByLinkId(savedGithubLink.getId());

        assertThat(actualResult1).isTrue();

        boolean actualResult2 = chatLinkRepository.existsByLinkId(savedOverflowLink.getId());

        assertThat(actualResult2).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void findChatLinkByChatIdAndLinkUrl() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedChat = chatRepository.save(CHAT);

        ChatLink chatLink = new ChatLink();
        chatLink.setChat(savedChat);
        chatLink.setLink(savedGithubLink);

        var savedChatLink = chatLinkRepository.save(chatLink);

        Optional<ChatLink> actualResult =
            chatLinkRepository.findChatLinkByChatIdAndLinkUrl(savedChat.getId(), savedGithubLink.getUrl());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(savedChatLink);
    }

    @Test
    @Transactional
    @Rollback
    void findAllLinksByChatId() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);
        var savedChat = chatRepository.save(CHAT);

        var links = List.of(savedGithubLink, savedOverflowLink);

        for (Link link : links) {
            ChatLink chatLink = new ChatLink();
            chatLink.setChat(savedChat);
            chatLink.setLink(link);

            chatLinkRepository.save(chatLink);
        }

        List<Link> actualResult = chatLinkRepository.findAllLinksByChatId(savedChat.getId());
        assertThat(actualResult).hasSize(2);
        assertThat(actualResult).containsAll(links);
    }

    @Test
    @Transactional
    @Rollback
    void findAllChatIdsByLinkId() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedChat1 = chatRepository.save(CHAT);
        var savedChat2 = chatRepository.save(SECOND_CHAT);

        var chats = List.of(savedChat1, savedChat2);

        for (Chat chat : chats) {
            ChatLink chatLink = new ChatLink();
            chatLink.setChat(chat);
            chatLink.setLink(savedGithubLink);

            chatLinkRepository.save(chatLink);
        }

        List<Long> actualResult = chatLinkRepository.findAllChatIdsByLinkId(savedGithubLink.getId());

        assertThat(actualResult).hasSize(2);
        assertThat(actualResult).containsAll(chats.stream().map(Chat::getId).toList());
    }
}
