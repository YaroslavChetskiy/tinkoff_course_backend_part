package edu.java.domain.repository;

import edu.java.dto.entity.Question;

public interface QuestionRepository {

    Question saveQuestion(Question question);

    Question findByLinkId(Long linkId);
}
