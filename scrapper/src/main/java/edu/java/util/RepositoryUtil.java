package edu.java.util;

import edu.java.dto.entity.Link;
import edu.java.dto.entity.LinkType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RepositoryUtil {

    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static Link buildLinkFromResultSet(ResultSet resultSet) throws SQLException {
        return new Link(
            resultSet.getLong("id"),
            resultSet.getString("url"),
            LinkType.find(resultSet.getString("type")),
            resultSet.getDate("checked_at")
                .toLocalDate()
                .atStartOfDay()
                .atOffset(ZONE_OFFSET),
            resultSet.getDate("last_updated_at")
                .toLocalDate()
                .atStartOfDay()
                .atOffset(ZONE_OFFSET),
            resultSet.getDate("created_at")
                .toLocalDate()
                .atStartOfDay()
                .atOffset(ZONE_OFFSET)
        );
    }
}
