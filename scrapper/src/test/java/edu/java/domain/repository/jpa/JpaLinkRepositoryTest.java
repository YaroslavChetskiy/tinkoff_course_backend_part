package edu.java.domain.repository.jpa;

import edu.java.dto.entity.hibernate.link.GithubRepositoryLink;
import edu.java.dto.entity.hibernate.link.Link;
import edu.java.dto.entity.hibernate.link.StackOverflowQuestionLink;
import edu.java.scrapper.IntegrationTest;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
class JpaLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JpaLinkRepository linkRepository;


    @Autowired
    private EntityManager entityManager;

    private static final Link GITHUB_LINK = GithubRepositoryLink.builder()
        .id(1L)
        .url("github.com/dummy/dummy_repo")
        .chatLinks(new ArrayList<>())
        .build();

    private static final Link STACKOVERFLOW_LINK = StackOverflowQuestionLink.builder()
        .id(2L)
        .url("https://stackoverflow.com/questions/123/dummy")
        .answerCount(1)
        .chatLinks(new ArrayList<>())
        .build();

    @Test
    @Transactional
    @Rollback
    void saveAndFind() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedStackOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);

        List<Link> links = linkRepository.findAll();

        assertThat(links).hasSize(2);
        assertThat(links).containsAll(List.of(savedGithubLink, savedStackOverflowLink));
    }

    @Test
    @Transactional
    @Rollback
    void delete() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedStackOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);

        linkRepository.delete(savedGithubLink);

        List<Link> links = linkRepository.findAll();

        assertThat(links).hasSize(1);
        assertThat(links).contains(savedStackOverflowLink);

        linkRepository.delete(savedStackOverflowLink);

        Optional<Link> deletedLink = linkRepository.findByUrl(savedStackOverflowLink.getUrl());

        assertThat(deletedLink).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void updateLastUpdatedAtAndCheckedAtByUrl() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);

        OffsetDateTime offsetDateTime = LocalDate.now()
            .minusDays(2L)
            .atStartOfDay()
            .atOffset(OffsetDateTime.now().getOffset());

        linkRepository.updateLastUpdatedAtAndCheckedAtByUrl(savedGithubLink.getUrl(), offsetDateTime, offsetDateTime);

        entityManager.clear();
        Optional<Link> linkOptional = linkRepository.findByUrl(savedGithubLink.getUrl());

        assertThat(linkOptional).isPresent();
        assertThat(linkOptional.get().getLastUpdatedAt()).isEqualTo(offsetDateTime);
        assertThat(linkOptional.get().getCheckedAt()).isEqualTo(offsetDateTime);
    }

    @Test
    @Transactional
    @Rollback
    void findOutdatedLinks() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);
        var savedStackOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);

        OffsetDateTime offsetDateTime = LocalDate.now()
            .minusDays(2L)
            .atStartOfDay()
            .atOffset(OffsetDateTime.now().getOffset());

        List<Link> links = List.of(savedGithubLink, savedStackOverflowLink);
        for (Link link : links) {
            linkRepository.updateLastUpdatedAtAndCheckedAtByUrl(link.getUrl(), offsetDateTime, offsetDateTime);
        }

        entityManager.clear();
        var outdatedLinks = linkRepository.findOutdatedLinks(OffsetDateTime.now().minusDays(1L));

        assertThat(outdatedLinks).hasSize(2);
        assertThat(outdatedLinks).containsAll(links);
    }

    @Test
    @Transactional
    @Rollback
    void findByUrl() {
        var savedGithubLink = linkRepository.save(GITHUB_LINK);

        Optional<Link> githubLinkOptional = linkRepository.findByUrl(savedGithubLink.getUrl());

        assertThat(githubLinkOptional).isPresent();
        assertThat(githubLinkOptional.get()).isEqualTo(savedGithubLink);

        Optional<Link> stackOverflowLinkOptional = linkRepository.findByUrl(STACKOVERFLOW_LINK.getUrl());

        assertThat(stackOverflowLinkOptional).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void updateAnswerCountByUrl() {
        var savedStackOverflowLink = linkRepository.save(STACKOVERFLOW_LINK);

        linkRepository.updateAnswerCountByUrl(savedStackOverflowLink.getUrl(), 2);

        entityManager.clear();
        Optional<Link> linkOptional = linkRepository.findByUrl(savedStackOverflowLink.getUrl());

        assertThat(linkOptional).isPresent();
        assertThat(((StackOverflowQuestionLink) (linkOptional.get())).getAnswerCount()).isEqualTo(2);
    }
}
