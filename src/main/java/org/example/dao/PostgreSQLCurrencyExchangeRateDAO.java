package org.example.dao;

import org.example.entities.CurrencyExchangeRateEntity;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class PostgreSQLCurrencyExchangeRateDAO implements CurrencyExchangeRateDAO {
    @Override
    public void create(Connection connection, CurrencyExchangeRateEntity currencyExchangeRateEntity)
            throws SQLException, NullPointerException
    {
        int id;

        try (
                ResultSet resultSet = connection.createStatement().executeQuery("""
                        SELECT NEXTVAL('currency_exchange_rates_sequence') AS id;
                """)
        ) {
            if (!resultSet.next()) {
                throw new SQLException("The entity id creation error.");
            }

            id = resultSet.getInt("id");
        }

        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        INSERT INTO currency_exchange_rates (
                            id,
                            scraper_id,
                            unit,
                            unit_currency_code,
                            rate_currency_code,
                            buy_rate,
                            sale_rate,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?
                        );
                """)
        ) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, Objects.requireNonNull(currencyExchangeRateEntity.getScraperId()));
            preparedStatement.setInt(3, Objects.requireNonNull(currencyExchangeRateEntity.getUnit()));
            preparedStatement.setString(4, Objects.requireNonNull(currencyExchangeRateEntity.getUnitCurrencyCode()));
            preparedStatement.setString(5, Objects.requireNonNull(currencyExchangeRateEntity.getRateCurrencyCode()));
            preparedStatement.setDouble(6, Objects.requireNonNull(currencyExchangeRateEntity.getBuyRate()));
            preparedStatement.setDouble(7, Objects.requireNonNull(currencyExchangeRateEntity.getSaleRate()));
            preparedStatement.setTimestamp(8, Timestamp.valueOf(Objects.requireNonNull(currencyExchangeRateEntity.getCreatedAt())));
            preparedStatement.setTimestamp(9, Timestamp.valueOf(Objects.requireNonNull(currencyExchangeRateEntity.getUpdatedAt())));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("The entity creation error.");
            }

            currencyExchangeRateEntity.setId(id);
        }
    }

    @Override
    public Optional<CurrencyExchangeRateEntity> read(Connection connection, CurrencyExchangeRateEntity currencyExchangeRateEntity)
            throws SQLException, NullPointerException
    {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        SELECT id,
                        	unit,
                        	buy_rate,
                        	sale_rate,
                        	created_at,
                        	updated_at
                        FROM currency_exchange_rates
                        WHERE scraper_id = ?
                        	AND unit_currency_code = ?
                        	AND rate_currency_code = ?;
                """)
        ) {
            preparedStatement.setInt(1, Objects.requireNonNull(currencyExchangeRateEntity.getScraperId()));
            preparedStatement.setString(2, Objects.requireNonNull(currencyExchangeRateEntity.getUnitCurrencyCode()));
            preparedStatement.setString(3, Objects.requireNonNull(currencyExchangeRateEntity.getRateCurrencyCode()));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(
                                new CurrencyExchangeRateEntity(
                                        resultSet.getInt("id"),
                                        currencyExchangeRateEntity.getScraperId(),
                                        resultSet.getInt("unit"),
                                        currencyExchangeRateEntity.getUnitCurrencyCode(),
                                        currencyExchangeRateEntity.getRateCurrencyCode(),
                                        resultSet.getDouble("buy_rate"),
                                        resultSet.getDouble("sale_rate"),
                                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                                        resultSet.getTimestamp("updated_at").toLocalDateTime()
                                )
                        )
                        : Optional.empty();
            }
        }
    }

    @Override
    public void update(Connection connection, CurrencyExchangeRateEntity currencyExchangeRateEntity)
            throws SQLException, NullPointerException
    {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        UPDATE currency_exchange_rates
                        SET unit = ?,
                        	buy_rate = ?,
                        	sale_rate = ?,
                        	updated_at = ?
                        WHERE id = ?;
                """)
        ) {
            preparedStatement.setInt(1, Objects.requireNonNull(currencyExchangeRateEntity.getUnit()));
            preparedStatement.setDouble(2, Objects.requireNonNull(currencyExchangeRateEntity.getBuyRate()));
            preparedStatement.setDouble(3, Objects.requireNonNull(currencyExchangeRateEntity.getSaleRate()));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(Objects.requireNonNull(currencyExchangeRateEntity.getUpdatedAt())));
            preparedStatement.setInt(5, Objects.requireNonNull(currencyExchangeRateEntity.getId()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("The entity updating error.");
            }
        }
    }
}