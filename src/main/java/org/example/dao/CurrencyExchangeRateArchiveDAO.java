package org.example.dao;

import org.example.entities.CurrencyExchangeRateArchiveEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface CurrencyExchangeRateArchiveDAO {
    /**
     * Adding the entity into the database.
     * @param connection Database connection.
     * @param currencyExchangeRateArchiveEntity Instance of the entity.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If one of the required entity fields is null.
     */
    void create(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws SQLException, NullPointerException;

    /**
     * Reading the entity from the database.
     * @param connection Database connection.
     * @param currencyExchangeRateArchiveEntity Instance of the entity, that will be used for search.
     * @return Optional with the instance of the entity, or empty Optional, if that entity wasn't found.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If one of the required entity fields is null.
     */
    Optional<CurrencyExchangeRateArchiveEntity> read(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws SQLException, NullPointerException;

    /**
     * Updating the entity in the database.
     * @param connection Database connection.
     * @param currencyExchangeRateArchiveEntity Instance of the entity, that will be updated.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If one of the required entity fields is null.
     */
    void update(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws SQLException, NullPointerException;
}
