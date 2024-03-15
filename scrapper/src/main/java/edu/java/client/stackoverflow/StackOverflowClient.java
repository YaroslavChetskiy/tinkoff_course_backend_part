package edu.java.client.stackoverflow;

import edu.java.dto.entity.Link;
import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.update.UpdateInfo;

public interface StackOverflowClient {

    QuestionResponse fetchQuestion(Long questionId);

    UpdateInfo checkForUpdate(Link link, int answerCount);

    Long getQuestionId(String url);
}
