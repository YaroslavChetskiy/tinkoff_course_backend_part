package edu.java.domain.repository.sqlRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatSqlRequest {

    public static final String SAVE_CHAT_SQL = """
        INSERT INTO scrapper_schema.chat
            (id)
        VALUES
            (?)
        """;

    public static final String DELETE_CHAT_SQL = """
        DELETE FROM scrapper_schema.chat
        WHERE id = ?
        """;

    public static final String FIND_BY_ID_SQL = """
        SELECT id, created_at FROM scrapper_schema.chat
        WHERE id = ?
        """;
}
