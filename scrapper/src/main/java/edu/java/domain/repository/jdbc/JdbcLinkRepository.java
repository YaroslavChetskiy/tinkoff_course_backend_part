package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.LinkRepository;
import edu.java.dto.entity.jdbc.Link;
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
import static edu.java.domain.repository.sqlRequest.LinkSqlRequest.DELETE_LINK_SQL;
import static edu.java.domain.repository.sqlRequest.LinkSqlRequest.FIND_LINK_BY_URL_SQL;
import static edu.java.domain.repository.sqlRequest.LinkSqlRequest.FIND_OUTDATED_LINKS;
import static edu.java.domain.repository.sqlRequest.LinkSqlRequest.SAVE_LINK_SQL;
import static edu.java.domain.repository.sqlRequest.LinkSqlRequest.UPDATE_LAST_UPDATE_AND_CHECK_TIME_SQL;

@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Link saveLink(Link link) {
        jdbcTemplate.update(SAVE_LINK_SQL, link.getUrl(), link.getType().name());
        return findLinkByUrl(link.getUrl());
    }

    @Override
    public Link deleteLink(String url) {
        var deletedLink = findLinkByUrl(url);
        jdbcTemplate.update(DELETE_LINK_SQL, url);
        return deletedLink;
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
