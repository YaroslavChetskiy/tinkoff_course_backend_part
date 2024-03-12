package edu.java.domain.repository.jdbc;

import edu.java.domain.repository.QuestionRepository;
import edu.java.dto.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static edu.java.domain.repository.sqlRequest.QuestionSqlRequest.FIND_BY_LINK_ID_SQL;
import static edu.java.domain.repository.sqlRequest.QuestionSqlRequest.SAVE_QUESTION_SQL;

@Repository
@RequiredArgsConstructor
public class JdbcQuestionRepository implements QuestionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Question saveQuestion(Question question) {
        jdbcTemplate.update(SAVE_QUESTION_SQL, question.getAnswerCount(), question.getLinkId());
        return findByLinkId(question.getLinkId());
    }

    @Override
    public Question findByLinkId(Long linkId) {
        try {
            return jdbcTemplate.queryForObject(
                FIND_BY_LINK_ID_SQL,
                (rs, rowNum) -> new Question(
                    rs.getLong("id"),
                    rs.getInt("answer_count"),
                    rs.getLong("link_id")
                ),
                linkId
            );
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }
}
