package edu.java.domain.repository.jdbc;

import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import edu.java.dto.entity.Question;
import edu.java.scrapper.IntegrationTest;
import edu.java.util.RepositoryUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbcQuestionRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcLinkRepository linkRepository;

    @Autowired
    private JdbcQuestionRepository questionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @Rollback
    void saveQuestion() {
        Link link = new Link(
            1L, "github.com/dummy/dummy_repo", LinkType.GITHUB_REPO,
            null, null, null
        );

        Link savedLink = linkRepository.saveLink(link);

        Question question = new Question(null, 2, savedLink.getId());
        var savedQuestion = questionRepository.saveQuestion(question);

        assertThat(savedQuestion.getAnswerCount()).isEqualTo(question.getAnswerCount());
        assertThat(savedQuestion.getLinkId()).isEqualTo(savedLink.getId());

        var questions = getQuestion();
        assertThat(questions).hasSize(1);
        assertThat(questions).contains(savedQuestion);
    }

    @Test
    @Transactional
    @Rollback
    void findByLinkId() {
        Link link = new Link(
            1L, "github.com/dummy/dummy_repo", LinkType.GITHUB_REPO,
            null, null, null
        );

        Link savedLink = linkRepository.saveLink(link);

        var nullQuestion = questionRepository.findByLinkId(savedLink.getId());
        assertThat(nullQuestion).isNull();

        Question question = new Question(null, 2, savedLink.getId());
        var savedQuestion = questionRepository.saveQuestion(question);

        Question actualResult = questionRepository.findByLinkId(savedLink.getId());

        assertThat(actualResult).isEqualTo(savedQuestion);
    }

    private List<Question> getQuestion() {
        return jdbcTemplate.query(
            "SELECT * FROM scrapper_schema.question",
            (rs, rowNum) -> new Question(
                rs.getLong("id"),
                rs.getInt("answer_count"),
                rs.getLong("link_id")
            )
        );
    }

}
