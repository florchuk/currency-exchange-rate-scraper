package org.example.seeders;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class PostgreSQLSeeder implements Seeder {
    @Override
    public void up(Connection connection) throws IOException, SQLException, NullPointerException {
        try (
                InputStream inputStream =
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("seeders/postgresql.sql");
                PreparedStatement preparedStatement =
                        connection.prepareStatement(new String(Objects.requireNonNull(inputStream).readAllBytes()))
        ) {
            preparedStatement.executeUpdate();
        }
    }
}