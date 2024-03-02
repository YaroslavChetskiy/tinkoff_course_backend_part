package edu.java.scrapper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationLiquibaseSetupTest extends IntegrationTest {

    private static final String SCHEMA = "scrapper_schema";

    private static final List<String> TABLE_NAMES = List.of("link", "chat", "chat_link");

    @Test
    public void checkExistingOfTables() {
        try {
            Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
            );

            assertThat(connection).isNotNull();
            assertThat(connection.isClosed()).isFalse();

            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet resultSet = metaData.getTables(
                null,
                SCHEMA,
                null,
                new String[] {"TABLE"}
            );

            List<String> existingTables = new ArrayList<>();

            while (resultSet.next()) {
                existingTables.add(resultSet.getString("TABLE_NAME"));
            }

            assertThat(existingTables).containsAll(TABLE_NAMES);

            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
