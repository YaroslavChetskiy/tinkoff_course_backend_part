package edu.java.domain.repository.jdbc;

import edu.java.dto.entity.jdbc.Link;
import edu.java.dto.entity.jdbc.LinkType;
import edu.java.dto.entity.jdbc.Question;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @Transactional
    @Rollback
    void updateAnswerCountByLinkId() {
        Link link = new Link(
            2L, "stackoverflow.com/questions/123", LinkType.GITHUB_REPO,
            null, null, null
        );

        Link savedLink = linkRepository.saveLink(link);
        Question question = new Question(2L, 1, savedLink.getId());
        questionRepository.saveQuestion(question);

        questionRepository.updateAnswerCountByLinkId(savedLink.getId(), 2);

        var updatedQuestion = questionRepository.findByLinkId(savedLink.getId());
        assertThat(updatedQuestion.getAnswerCount()).isEqualTo(2);
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
