package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.ChatLinkRepository;
import edu.java.dto.entity.jdbc.Link;
import edu.java.util.RepositoryUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static edu.java.domain.repository.sqlRequest.ChatLinkSqlRequest.ADD_LINK_TO_CHAT_SQL;
import static edu.java.domain.repository.sqlRequest.ChatLinkSqlRequest.FIND_ALL_CHAT_IDS_BY_LINK_ID_SQL;
import static edu.java.domain.repository.sqlRequest.ChatLinkSqlRequest.FIND_ALL_LINKS_BY_CHAT_ID_SQL;
import static edu.java.domain.repository.sqlRequest.ChatLinkSqlRequest.IS_LINK_TRACKED_IN_CHAT_SQL;
import static edu.java.domain.repository.sqlRequest.ChatLinkSqlRequest.IS_LINK_TRACKED_SQL;
import static edu.java.domain.repository.sqlRequest.ChatLinkSqlRequest.REMOVE_LINK_FROM_CHAT_SQL;

@Repository
@RequiredArgsConstructor
public class JdbcChatLinkRepository implements ChatLinkRepository {

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
