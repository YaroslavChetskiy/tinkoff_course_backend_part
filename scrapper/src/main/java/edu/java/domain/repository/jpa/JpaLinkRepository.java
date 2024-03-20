package edu.java.domain.repository.jpa;

import edu.java.dto.entity.hibernate.link.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    @Modifying
    @Query("UPDATE Link SET lastUpdatedAt = :lastUpdatedAt, checkedAt = :checkedAt WHERE url = :url")
    void updateLastUpdatedAtAndCheckedAtByUrl(String url, OffsetDateTime lastUpdatedAt, OffsetDateTime checkedAt);

    @Modifying
    @Query("UPDATE StackOverflowQuestionLink SET answerCount = :answerCount WHERE url = :url")
    void updateAnswerCountByUrl(String url, Integer answerCount);

    @Query("SELECT l FROM Link l WHERE l.checkedAt < :threshold")
    List<Link> findOutdatedLinks(OffsetDateTime threshold);

    Optional<Link> findByUrl(String url);
}
