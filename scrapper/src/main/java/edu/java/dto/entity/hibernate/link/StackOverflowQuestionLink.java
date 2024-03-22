package edu.java.dto.entity.hibernate.link;

import edu.java.dto.entity.hibernate.ChatLink;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue("STACKOVERFLOW_QUESTION")
public class StackOverflowQuestionLink extends Link {

    private Integer answerCount;

    @Builder
    public StackOverflowQuestionLink(
        Long id,
        String url,
        OffsetDateTime checkedAt,
        OffsetDateTime lastUpdatedAt,
        OffsetDateTime createdAt,
        List<ChatLink> chatLinks,
        Integer answerCount
    ) {
        super(id, url, checkedAt, lastUpdatedAt, createdAt, chatLinks);
        this.answerCount = answerCount;
    }
}
