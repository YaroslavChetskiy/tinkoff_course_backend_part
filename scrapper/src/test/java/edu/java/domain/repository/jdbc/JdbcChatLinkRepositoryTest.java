package edu.java.domain.repository.jdbc;

import edu.java.dto.entity.jdbc.Chat;
import edu.java.dto.entity.jdbc.ChatLink;
import edu.java.dto.entity.jdbc.Link;
import edu.java.dto.entity.jdbc.LinkType;
import edu.java.scrapper.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Rollback
@Transactional
@DirtiesContext
class JdbcChatLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcChatLinkRepository chatLinkRepository;

    @Autowired
    private JdbcChatRepository chatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Link LINK = new Link(
        1L, "github.com/dummy/dummy_repo", LinkType.GITHUB_REPO,
        null, null, null
    );

    private static final Link SECOND_LINK = new Link(
        2L, "github.com/dummy/dummy2_repo", LinkType.GITHUB_REPO,
        null, null, null
    );

    private static final Chat CHAT = new Chat(255L, null);
    private static final Chat SECOND_CHAT = new Chat(100L, null);

    @Test
    @Transactional
    @Rollback
    void addLinkToChat() {
        saveLinkWithId(LINK);
        chatRepository.saveChat(CHAT);

        chatLinkRepository.addLinkToChat(CHAT.getId(), LINK.getId());

        var chatLinks = getChatLinks();

        assertThat(chatLinks).hasSize(1);
        assertThat(chatLinks.getFirst().getChatId()).isEqualTo(CHAT.getId());
        assertThat(chatLinks.getFirst().getLinkId()).isEqualTo(LINK.getId());
    }

    @Test
    @Transactional
    @Rollback
    void removeLinkFromChat() {
        chatRepository.saveChat(CHAT);
        saveLinkWithId(LINK);
        saveLinkWithId(SECOND_LINK);
        chatLinkRepository.addLinkToChat(CHAT.getId(), LINK.getId());
        chatLinkRepository.addLinkToChat(CHAT.getId(), SECOND_LINK.getId());

        chatLinkRepository.removeLinkFromChat(CHAT.getId(), LINK.getId());

        var chatLinks = getChatLinks();

        assertThat(chatLinks).hasSize(1);
        assertThat(chatLinks.getFirst().getChatId()).isEqualTo(CHAT.getId());
        assertThat(chatLinks.getFirst().getLinkId()).isEqualTo(SECOND_LINK.getId());
    }

    @Test
    @Transactional
    @Rollback
    void isLinkTrackedInChat() {
        chatRepository.saveChat(CHAT);
        saveLinkWithId(LINK);
        saveLinkWithId(SECOND_LINK);
        chatLinkRepository.addLinkToChat(CHAT.getId(), LINK.getId());

        boolean actualResult1 = chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), LINK.getId());

        assertThat(actualResult1).isTrue();

        boolean actualResult2 = chatLinkRepository.isLinkTrackedInChat(CHAT.getId(), SECOND_LINK.getId());

        assertThat(actualResult2).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void isLinkTracked() {
        chatRepository.saveChat(CHAT);
        saveLinkWithId(LINK);
        saveLinkWithId(SECOND_LINK);
        chatLinkRepository.addLinkToChat(CHAT.getId(), LINK.getId());

        boolean actualResult1 = chatLinkRepository.isLinkTracked(LINK.getId());

        assertThat(actualResult1).isTrue();

        boolean actualResult2 = chatLinkRepository.isLinkTracked(SECOND_LINK.getId());

        assertThat(actualResult2).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void findAllLinksByChatId() {
        chatRepository.saveChat(CHAT);
        saveLinkWithId(LINK);
        saveLinkWithId(SECOND_LINK);

        List<Link> emptyLinkList = chatLinkRepository.findAllLinksByChatId(CHAT.getId());

        assertThat(emptyLinkList).isEmpty();

        chatLinkRepository.addLinkToChat(CHAT.getId(), LINK.getId());
        chatLinkRepository.addLinkToChat(CHAT.getId(), SECOND_LINK.getId());

        List<Link> notEmptyLinkList = chatLinkRepository.findAllLinksByChatId(CHAT.getId());

        assertThat(notEmptyLinkList).isNotEmpty();
        assertThat(notEmptyLinkList).hasSize(2);
        assertThat(notEmptyLinkList.getFirst().getUrl()).isEqualTo(LINK.getUrl());
        assertThat(notEmptyLinkList.getLast().getUrl()).isEqualTo(SECOND_LINK.getUrl());
    }

    @Test
    @Transactional
    @Rollback
    void findAllChatIdsByLinkId() {
        var chats = List.of(CHAT, SECOND_CHAT);
        saveLinkWithId(LINK);
        for (Chat chat : chats) {
            chatRepository.saveChat(chat);
            chatLinkRepository.addLinkToChat(chat.getId(), LINK.getId());
        }

        List<Long> chatIds = chatLinkRepository.findAllChatIdsByLinkId(LINK.getId());

        assertThat(chatIds).hasSize(2);
        assertThat(chatIds).containsAll(chats.stream().map(Chat::getId).toList());
    }

    private List<ChatLink> getChatLinks() {
        return jdbcTemplate.query(
            "SELECT id, chat_id, link_id FROM scrapper_schema.chat_link",
            (rs, rowNum) -> new ChatLink(
                rs.getLong("id"),
                rs.getLong("chat_id"),
                rs.getLong("link_id")
            )
        );
    }

    private void saveLinkWithId(Link link) {
        jdbcTemplate.update("""
                INSERT INTO scrapper_schema.link
                    (id, url, type)
                    VALUES (?, ?, ?)
            """, link.getId(), link.getUrl(), link.getType().name());
    }
}
