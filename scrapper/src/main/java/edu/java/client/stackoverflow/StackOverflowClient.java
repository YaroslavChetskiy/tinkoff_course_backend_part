package edu.java.client.stackoverflow;

import edu.java.dto.stackoverflow.QuestionResponse;

public interface StackOverflowClient {

    QuestionResponse fetchQuestion(Long questionId);
}
