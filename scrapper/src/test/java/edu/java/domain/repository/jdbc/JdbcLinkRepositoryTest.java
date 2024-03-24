package edu.java.domain.repository.jdbc;

import edu.java.configuration.ApplicationConfig;
import edu.java.configuration.ClientConfiguration;
import edu.java.dto.entity.jdbc.Link;
import edu.java.dto.entity.jdbc.LinkType;
import edu.java.scrapper.IntegrationTest;
import edu.java.util.RepositoryUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class JdbcLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcLinkRepository linkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Link LINK = new Link(
        1L, "github.com/dummy/dummy_repo", LinkType.GITHUB_REPO,
        null, null, null
    );

    private static final Link SECOND_LINK = new Link(
        2L, "github.com/dummy/dummy2_repo", LinkType.GITHUB_REPO,
        null, null, null
    );

    @Test
    @Transactional
    @Rollback
    void saveLink() {
        linkRepository.saveLink(LINK);
        linkRepository.saveLink(SECOND_LINK);

        var links = getLinks();

        assertThat(links).hasSize(2);
        assertThat(links.getFirst().getUrl()).isEqualTo(LINK.getUrl());
        assertThat(links.getLast().getUrl()).isEqualTo(SECOND_LINK.getUrl());
    }

    @Test
    @Transactional
    @Rollback
    void deleteLink() {
        linkRepository.saveLink(LINK);
        linkRepository.saveLink(SECOND_LINK);

        linkRepository.deleteLink(SECOND_LINK.getUrl());

        var links = getLinks();

        assertThat(links).hasSize(1);
        assertThat(links.getFirst().getUrl()).isEqualTo(LINK.getUrl());

        linkRepository.deleteLink(LINK.getUrl());

        links = getLinks();

        assertThat(links).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void findLinkByUrl() {
        linkRepository.saveLink(LINK);

        Link link1 = linkRepository.findLinkByUrl(LINK.getUrl());

        assertThat(link1).isNotNull();

        Link link2 = linkRepository.findLinkByUrl(SECOND_LINK.getUrl());

        assertThat(link2).isNull();
    }

    @Test
    @Transactional
    @Rollback
    void updateLastUpdateAndCheckTime() {
        linkRepository.saveLink(LINK);

        OffsetDateTime offsetDateTime = LocalDate.now().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

        linkRepository.updateLastUpdateAndCheckTime(LINK.getUrl(), offsetDateTime, offsetDateTime);

        var link = linkRepository.findLinkByUrl(LINK.getUrl());

        assertThat(link.getLastUpdatedAt()).isEqualTo(offsetDateTime);
        assertThat(link.getCheckedAt()).isEqualTo(offsetDateTime);
    }

    @Test
    @Transactional
    @Rollback
    void findOutdatedLinks() {
        linkRepository.saveLink(LINK);

        OffsetDateTime offsetDateTime = LocalDate.now()
            .minusDays(2L)
            .atStartOfDay()
            .atOffset(OffsetDateTime.now().getOffset());

        linkRepository.updateLastUpdateAndCheckTime(LINK.getUrl(), offsetDateTime, offsetDateTime);

        List<Link> link = linkRepository.findOutdatedLinks(Duration.ofDays(1L));

        assertThat(link).hasSize(1);
        assertThat(link.getFirst().getUrl()).isEqualTo(LINK.getUrl());
    }

    private List<Link> getLinks() {
        return jdbcTemplate.query(
            "SELECT * FROM scrapper_schema.link",
            (rs, rowNum) -> RepositoryUtil.buildLinkFromResultSet(rs)
        );
    }
}
