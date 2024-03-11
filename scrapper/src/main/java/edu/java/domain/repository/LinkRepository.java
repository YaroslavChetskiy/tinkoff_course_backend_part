package edu.java.domain.repository;

import edu.java.dto.entity.Link;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {

    Link saveLink(Link link);

    Link deleteLink(String url);

    void updateLastUpdateAndCheckTime(String url, OffsetDateTime lastUpdatedAt, OffsetDateTime checkedAt);

    Link findLinkByUrl(String url);

    List<Link> findOutdatedLinks(Duration threshold);
}
