package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.ChatRepository;
import edu.java.dto.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static edu.java.util.RepositoryUtil.ZONE_OFFSET;

@Repository
@RequiredArgsConstructor
public class JdbcChatRepository implements ChatRepository {

    private static final String SAVE_CHAT_SQL = """
        INSERT INTO scrapper_schema.chat
            (id)
        VALUES
            (?)
        """;

    private static final String DELETE_CHAT_SQL = """
        DELETE FROM scrapper_schema.chat
        WHERE id = ?
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT id, created_at FROM scrapper_schema.chat
        WHERE id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveChat(Chat chat) {
        jdbcTemplate.update(SAVE_CHAT_SQL, chat.getId());
    }

    @Override
    public void deleteChat(Long chatId) {
        jdbcTemplate.update(DELETE_CHAT_SQL, chatId);
    }

    @Override
    public Chat findById(Long chatId) {
        try {
            return jdbcTemplate.queryForObject(
                FIND_BY_ID_SQL,
                (rs, rowNum) -> new Chat(
                    rs.getLong("id"),
                    rs.getDate("created_at")
                        .toLocalDate()
                        .atStartOfDay()
                        .atOffset(ZONE_OFFSET)
                ),
                chatId
            );
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }
}
