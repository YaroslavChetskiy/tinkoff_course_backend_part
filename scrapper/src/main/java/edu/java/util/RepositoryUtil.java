package edu.java.util;

import edu.java.dto.entity.jdbc.Link;
import edu.java.dto.entity.jdbc.LinkType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RepositoryUtil {

    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static Link buildLinkFromResultSet(ResultSet resultSet) throws SQLException {
        var checkedAt = resultSet.getTimestamp("checked_at");
        var lastUpdatedAt = resultSet.getTimestamp("last_updated_at");
        var createdAt = resultSet.getTimestamp("created_at");
        // я не уверен, что null возможно, так как на стороне БД устанавливаются DEFAULT значения
        return new Link(
            resultSet.getLong("id"),
            resultSet.getString("url"),
            LinkType.find(resultSet.getString("type")),
            checkedAt != null
                ? checkedAt.toLocalDateTime().atOffset(ZONE_OFFSET)
                : null,
            lastUpdatedAt != null
                ? lastUpdatedAt.toLocalDateTime().atOffset(ZONE_OFFSET)
                : null,
            createdAt != null
                ? createdAt.toLocalDateTime().atOffset(ZONE_OFFSET)
                : null
        );
    }
}
