package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.ChatLinkRepository;
import edu.java.dto.entity.Link;
import edu.java.util.RepositoryUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcChatLinkRepository implements ChatLinkRepository {

    private static final String ADD_LINK_TO_CHAT_SQL = """
        INSERT INTO scrapper_schema.chat_link
            (chat_id, link_id)
        VALUES (?, ?)
        """;

    private static final String REMOVE_LINK_FROM_CHAT_SQL = """
        DELETE FROM scrapper_schema.chat_link
        WHERE chat_id = ? AND link_id = ?
        """;

    private static final String IS_LINK_TRACKED_IN_CHAT_SQL = """
        SELECT EXISTS (
            SELECT 1 FROM scrapper_schema.chat_link
            WHERE chat_id = ? AND link_id = ?
        )
        """;

    private static final String IS_LINK_TRACKED_SQL = """
        SELECT EXISTS (
            SELECT 1 FROM scrapper_schema.chat_link
            WHERE link_id = ?
        )
        """;

    private static final String FIND_ALL_LINKS_BY_CHAT_ID_SQL = """
        SELECT
            l.id,
            l.url,
            l.type,
            l.checked_at,
            l.last_updated_at,
            l.created_at
        FROM scrapper_schema.link l
        JOIN
            scrapper_schema.chat_link cl ON l.id = cl.link_id
        WHERE cl.chat_id = ?
        """;

    private static final String FIND_ALL_CHAT_IDS_BY_LINK_ID_SQL = """
        SELECT chat_id FROM scrapper_schema.chat_link
        WHERE link_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLinkToChat(Long chatId, Long linkId) {
        jdbcTemplate.update(ADD_LINK_TO_CHAT_SQL, chatId, linkId);
    }

    @Override
    public void removeLinkFromChat(Long chatId, Long linkId) {
        jdbcTemplate.update(REMOVE_LINK_FROM_CHAT_SQL, chatId, linkId);
    }

    @Override
    public boolean isLinkTrackedInChat(Long chatId, Long linkId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
            IS_LINK_TRACKED_IN_CHAT_SQL,
            Boolean.class,
            chatId,
            linkId
        ));
    }

    @Override
    public boolean isLinkTracked(Long linkId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(IS_LINK_TRACKED_SQL, Boolean.class, linkId));
    }

    @Override
    public List<Link> findAllLinksByChatId(Long chatId) {
        return jdbcTemplate.query(
            FIND_ALL_LINKS_BY_CHAT_ID_SQL,
            (rs, rowNum) -> RepositoryUtil.buildLinkFromResultSet(rs),
            chatId
        );
    }

    @Override
    public List<Long> findAllChatIdsByLinkId(Long linkId) {
        return jdbcTemplate.queryForList(FIND_ALL_CHAT_IDS_BY_LINK_ID_SQL, Long.class, linkId);
    }
}
