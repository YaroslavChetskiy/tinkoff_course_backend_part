package edu.java.domain.repository.jooq;

import edu.java.dto.entity.jdbc.Chat;
import edu.java.scrapper.IntegrationTest;
import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static edu.java.domain.jooq.scrapper_schema.Tables.CHAT;
import static org.assertj.core.api.Assertions.assertThat;


// Так как JOOQ и JDBC похожи, то и тесты также почти идентичны
// Поэтому думаю излишне делать отдельные тесты для сервисов, ведь
// единственное их различие - реализация репозиториев
// Если будет лучше, могу переименовать тесты в JdbcAndJooq*Test и сделать тесты просто параметризованными
// В Hibernate будут отличаться методы репозиториев, поэтому для
// него, конечно, будут отдельные тесты
@SpringBootTest
@DirtiesContext
class JooqChatRepositoryTest extends IntegrationTest {

    @Autowired
    private JooqChatRepository chatRepository;

    @Autowired
    private DSLContext dslContext;

    private static final Chat TEST_CHAT = new Chat(255L, null);

    @Test
    @Transactional
    @Rollback
    void saveChat() {
        chatRepository.saveChat(TEST_CHAT);

        var chats = getChats();

        assertThat(chats).isNotEmpty();
        assertThat(chats).hasSize(1);
        assertThat(chats.getFirst().getId()).isEqualTo(TEST_CHAT.getId());
    }

    @Test
    @Transactional
    @Rollback
    void deleteChat() {
        chatRepository.saveChat(TEST_CHAT);

        chatRepository.deleteChat(TEST_CHAT.getId());

        var chats = getChats();

        assertThat(chats).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void findById() {
        var chat = chatRepository.findById(TEST_CHAT.getId());
        assertThat(chat).isNull();

        chatRepository.saveChat(TEST_CHAT);

        chat = chatRepository.findById(TEST_CHAT.getId());
        assertThat(chat).isNotNull();
    }

    private List<Chat> getChats() {
        return dslContext.select(CHAT.fields())
            .from(CHAT)
            .fetchInto(Chat.class);
    }

}
