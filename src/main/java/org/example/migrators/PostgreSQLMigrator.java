package org.example.migrators;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class PostgreSQLMigrator implements Migrator {
    @Override
    public void up(Connection connection) throws IOException, SQLException, NullPointerException {
        try (
                InputStream inputStream =
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("migrators/postgresql-up.sql");
                PreparedStatement preparedStatement =
                        connection.prepareStatement(new String(Objects.requireNonNull(inputStream).readAllBytes()))
        ) {
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void down(Connection connection) throws IOException, SQLException, NullPointerException {
        try (
                InputStream inputStream =
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("migrators/postgresql-down.sql");
                PreparedStatement preparedStatement =
                        connection.prepareStatement(new String(Objects.requireNonNull(inputStream).readAllBytes()))
        ) {
            preparedStatement.executeUpdate();
        }
    }
}