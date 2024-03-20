package edu.java.client.stackoverflow;

import edu.java.dto.stackoverflow.QuestionResponse;
import edu.java.dto.stackoverflow.QuestionResponse.ItemResponse;
import edu.java.dto.update.UpdateInfo;
import java.time.OffsetDateTime;

public interface StackOverflowClient {

    QuestionResponse fetchQuestion(Long questionId);

    UpdateInfo checkForUpdate(String url, OffsetDateTime lastUpdatedAt, int answerCount, ItemResponse question);

    Long getQuestionId(String url);
}
