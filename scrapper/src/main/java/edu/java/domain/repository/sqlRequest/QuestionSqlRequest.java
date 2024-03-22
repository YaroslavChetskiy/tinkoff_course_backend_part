package edu.java.domain.repository.sqlRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QuestionSqlRequest {

    public static final String SAVE_QUESTION_SQL = """
        INSERT INTO scrapper_schema.question
            (answer_count, link_id)
        VALUES
            (?, ?)
        """;

    public static final String FIND_BY_LINK_ID_SQL = """
        SELECT
            id,
            answer_count,
            link_id
        FROM scrapper_schema.question
        WHERE link_id = ?
        """;

    public static final String UPDATE_ANSWER_COUNT_BY_LINK_ID_SQL = """
        UPDATE scrapper_schema.question
        SET answer_count = ?
        WHERE link_id = ?
        """;
}
