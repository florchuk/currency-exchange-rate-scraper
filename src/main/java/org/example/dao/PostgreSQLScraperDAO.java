package org.example.dao;

import org.example.entities.ScraperEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class PostgreSQLScraperDAO implements ScraperDAO {
    @Override
    public Optional<ScraperEntity> read(Connection connection, ScraperEntity scraperEntity)
            throws SQLException, NullPointerException
    {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        SELECT id,
                        	name_en,
                        	name_uk,
                        	created_at,
                        	updated_at
                        FROM scrapers
                        WHERE class_name = ?;
                """)
        ) {
            preparedStatement.setString(1, Objects.requireNonNull(scraperEntity.getClazz()).getName());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(
                                new ScraperEntity(
                                        resultSet.getInt("id"),
                                        scraperEntity.getClazz(),
                                        resultSet.getString("name_en"),
                                        resultSet.getString("name_uk"),
                                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                                        resultSet.getTimestamp("updated_at").toLocalDateTime()
                                )
                        )
                        : Optional.empty();
            }
        }
    }
}