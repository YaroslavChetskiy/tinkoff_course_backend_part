package edu.java.domain.repository.sqlRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatLinkSqlRequest {

    public static final String ADD_LINK_TO_CHAT_SQL = """
        INSERT INTO scrapper_schema.chat_link
            (chat_id, link_id)
        VALUES (?, ?)
        """;

    public static final String REMOVE_LINK_FROM_CHAT_SQL = """
        DELETE FROM scrapper_schema.chat_link
        WHERE chat_id = ? AND link_id = ?
        """;

    public static final String IS_LINK_TRACKED_IN_CHAT_SQL = """
        SELECT EXISTS (
            SELECT 1 FROM scrapper_schema.chat_link
            WHERE chat_id = ? AND link_id = ?
        )
        """;

    public static final String IS_LINK_TRACKED_SQL = """
        SELECT EXISTS (
            SELECT 1 FROM scrapper_schema.chat_link
            WHERE link_id = ?
        )
        """;

    public static final String FIND_ALL_LINKS_BY_CHAT_ID_SQL = """
        SELECT
            l.id,
            l.url,
            l.type,
            l.checked_at,
            l.last_updated_at,
            l.created_at
        FROM scrapper_schema.link l
        JOIN
            scrapper_schema.chat_link cl ON l.id = cl.link_id
        WHERE cl.chat_id = ?
        """;

    public static final String FIND_ALL_CHAT_IDS_BY_LINK_ID_SQL = """
        SELECT chat_id FROM scrapper_schema.chat_link
        WHERE link_id = ?
        """;
}
