package edu.java.domain.repository.jdbc;

import edu.java.dto.entity.jdbc.Chat;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcChatRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcChatRepository chatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Chat CHAT = new Chat(255L, null);

    @Test
    @Transactional
    @Rollback
    void saveChat() {
        chatRepository.saveChat(CHAT);

        var chats = getChats();

        assertThat(chats).isNotEmpty();
        assertThat(chats).hasSize(1);
        assertThat(chats.getFirst().getId()).isEqualTo(CHAT.getId());
    }

    @Test
    @Transactional
    @Rollback
    void deleteChat() {
        chatRepository.saveChat(CHAT);

        chatRepository.deleteChat(CHAT.getId());

        var chats = getChats();

        assertThat(chats).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void findById() {
        var chat = chatRepository.findById(CHAT.getId());
        assertThat(chat).isNull();

        chatRepository.saveChat(CHAT);

        chat = chatRepository.findById(CHAT.getId());
        assertThat(chat).isNotNull();
    }

    private List<Chat> getChats() {
        return jdbcTemplate.query(
            "SELECT id, created_at FROM scrapper_schema.chat",
            (rs, rowNum) -> new Chat(
                rs.getLong("id"),
                rs.getDate("created_at").toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC)
            )
        );
    }
}
