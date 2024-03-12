package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.ChatRepository;
import edu.java.dto.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static edu.java.domain.repository.sqlRequest.ChatSqlRequest.DELETE_CHAT_SQL;
import static edu.java.domain.repository.sqlRequest.ChatSqlRequest.FIND_BY_ID_SQL;
import static edu.java.domain.repository.sqlRequest.ChatSqlRequest.SAVE_CHAT_SQL;
import static edu.java.util.RepositoryUtil.ZONE_OFFSET;

@Repository
@RequiredArgsConstructor
public class JdbcChatRepository implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Chat saveChat(Chat chat) {
        jdbcTemplate.update(SAVE_CHAT_SQL, chat.getId());
        return findById(chat.getId());
    }

    @Override
    public Chat deleteChat(Long chatId) {
        Chat deletedChat = findById(chatId);
        jdbcTemplate.update(DELETE_CHAT_SQL, chatId);
        return deletedChat;
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
