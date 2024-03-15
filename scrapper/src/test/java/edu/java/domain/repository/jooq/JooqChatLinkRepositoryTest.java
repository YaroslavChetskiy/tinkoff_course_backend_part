package edu.java.domain.repository.jooq;

import edu.java.domain.jooq.scrapper_schema.Tables;
import edu.java.dto.entity.Chat;
import edu.java.dto.entity.ChatLink;
import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.scrapper.IntegrationTest;
import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static edu.java.domain.jooq.scrapper_schema.Tables.CHAT_LINK;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JooqChatLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JooqChatLinkRepository chatLinkRepository;

    @Autowired
    private JooqChatRepository chatRepository;

    @Autowired
    private DSLContext dslContext;

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
        return dslContext.select(CHAT_LINK.fields())
            .from(CHAT_LINK)
            .fetchInto(ChatLink.class);
    }

    private void saveLinkWithId(Link link) {
        dslContext.insertInto(Tables.LINK)
            .set(Tables.LINK.ID, link.getId())
            .set(Tables.LINK.URL, link.getUrl())
            .set(Tables.LINK.TYPE, link.getType().name())
            .execute();
    }

}
