package edu.java.domain.repository.sqlRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkSqlRequest {

    public static final String SAVE_LINK_SQL = """
        INSERT INTO scrapper_schema.link
        (url, type)
        VALUES (?, ?)
        """;

    public static final String DELETE_LINK_SQL = """
        DELETE FROM scrapper_schema.link
        WHERE url = ?
        """;

    public static final String FIND_LINK_BY_URL_SQL = """
        SELECT
            id,
            url,
            type,
            checked_at,
            last_updated_at,
            created_at
        FROM scrapper_schema.link
        WHERE url = ?
        """;

    public static final String FIND_OUTDATED_LINKS = """
        SELECT
            id,
            url,
            type,
            checked_at,
            last_updated_at,
            created_at
        FROM scrapper_schema.link
        WHERE checked_at < ?
        """;

    public static final String UPDATE_LAST_UPDATE_AND_CHECK_TIME_SQL = """
        UPDATE scrapper_schema.link
        SET last_updated_at = ?, checked_at = ?
        WHERE url = ?
        """;
}
