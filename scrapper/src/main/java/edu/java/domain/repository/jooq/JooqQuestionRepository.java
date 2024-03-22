package edu.java.domain.repository.jooq;

import edu.java.domain.repository.QuestionRepository;
import edu.java.dto.entity.jdbc.Question;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import static edu.java.domain.jooq.scrapper_schema.Tables.QUESTION;

@Repository
@RequiredArgsConstructor
public class JooqQuestionRepository implements QuestionRepository {

    private final DSLContext dslContext;

    @Override
    public Question saveQuestion(Question question) {
        dslContext.insertInto(QUESTION)
            .set(QUESTION.ANSWER_COUNT, question.getAnswerCount())
            .set(QUESTION.LINK_ID, question.getLinkId())
            .execute();
        return findByLinkId(question.getLinkId());
    }

    @Override
    public Question findByLinkId(Long linkId) {
        return dslContext.select(QUESTION.fields())
            .from(QUESTION)
            .where(QUESTION.LINK_ID.eq(linkId))
            .fetchOneInto(Question.class);
    }

    @Override
    public void updateAnswerCountByLinkId(Long linkId, Integer answerCount) {
        dslContext.update(QUESTION)
            .set(QUESTION.ANSWER_COUNT, answerCount)
            .where(QUESTION.LINK_ID.eq(linkId))
            .execute();
    }
}
