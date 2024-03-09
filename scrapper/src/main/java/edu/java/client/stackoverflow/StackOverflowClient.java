package edu.java.client.stackoverflow;

import edu.java.dto.entity.Link;
import edu.java.dto.stackoverflow.QuestionResponse;
import java.time.OffsetDateTime;

public interface StackOverflowClient {

    QuestionResponse fetchQuestion(Long questionId);

    OffsetDateTime checkForUpdate(Link link);
}
