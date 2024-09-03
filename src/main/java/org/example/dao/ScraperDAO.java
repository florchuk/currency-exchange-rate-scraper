package org.example.dao;

import org.example.entities.ScraperEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface ScraperDAO {
    /**
     * Reading the entity from the database.
     * @param connection Database connection.
     * @param scraperEntity Instance of the entity, that will be used for search.
     * @return Optional with the instance of the entity, or empty Optional, if that entity wasn't found.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If one of the required entity fields is null.
     */
    Optional<ScraperEntity> read(Connection connection, ScraperEntity scraperEntity)
            throws SQLException, NullPointerException;
}