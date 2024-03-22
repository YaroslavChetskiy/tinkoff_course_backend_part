package edu.java.domain.repository;

import edu.java.dto.entity.jdbc.Question;

public interface QuestionRepository {

    Question saveQuestion(Question question);

    Question findByLinkId(Long linkId);

    void updateAnswerCountByLinkId(Long linkId, Integer answerCount);
}
