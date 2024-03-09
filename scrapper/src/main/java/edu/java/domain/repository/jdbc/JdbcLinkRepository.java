package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.LinkRepository;
import edu.java.dto.entity.Link;
import edu.java.util.RepositoryUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {
    private static final String SAVE_LINK_SQL = """
        INSERT INTO scrapper_schema.link
        (url, type)
        VALUES (?, ?)
        """;

    private static final String DELETE_LINK_SQL = """
        DELETE FROM scrapper_schema.link
        WHERE url = ?
        """;

    private static final String FIND_LINK_BY_URL_SQL = """
        SELECT
            id,
            url,
            type,
            checked_at,
            last_updated_at,
            created_at
        FROM scrapper_schema.link
        WHERE url = ?
        """;

    private static final String FIND_OUTDATED_LINKS = """
        SELECT
            id,
            url,
            type,
            checked_at,
            last_updated_at,
            created_at
        FROM scrapper_schema.link
        WHERE checked_at < ?
        """;

    private static final String UPDATE_LAST_UPDATE_AND_CHECK_TIME_SQL = """
        UPDATE scrapper_schema.link
        SET last_updated_at = ?, checked_at = ?
        WHERE url = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveLink(Link link) {
        jdbcTemplate.update(SAVE_LINK_SQL, link.getUrl(), link.getType().name());
    }

    @Override
    public void deleteLink(String url) {
        jdbcTemplate.update(DELETE_LINK_SQL, url);
    }

    @Override
    public void updateLastUpdateAndCheckTime(String url, OffsetDateTime lastUpdatedAt, OffsetDateTime checkedAt) {
        jdbcTemplate.update(UPDATE_LAST_UPDATE_AND_CHECK_TIME_SQL, lastUpdatedAt, checkedAt, url);
    }

    @Override
    public Link findLinkByUrl(String url) {
        try {
            return jdbcTemplate.queryForObject(
                FIND_LINK_BY_URL_SQL,
                (rs, rowNum) -> RepositoryUtil.buildLinkFromResultSet(rs),
                url
            );
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }

    @Override
    public List<Link> findOutdatedLinks(Duration threshold) {
        LocalDateTime thresholdDateTime = LocalDateTime.now().minus(threshold);
        OffsetDateTime thresholdOffsetDateTime = thresholdDateTime.atOffset(ZoneOffset.UTC);

        return jdbcTemplate.query(
            FIND_OUTDATED_LINKS,
            (rs, rowNum) -> RepositoryUtil.buildLinkFromResultSet(rs),
            thresholdOffsetDateTime
        );
    }


}
