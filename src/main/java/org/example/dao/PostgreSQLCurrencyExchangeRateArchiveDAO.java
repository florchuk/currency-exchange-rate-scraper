package org.example.dao;

import org.example.entities.CurrencyExchangeRateArchiveEntity;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class PostgreSQLCurrencyExchangeRateArchiveDAO implements CurrencyExchangeRateArchiveDAO {
    @Override
    public void create(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws SQLException, NullPointerException
    {
        long id;

        try (
                ResultSet resultSet = connection.createStatement().executeQuery("""
                        SELECT NEXTVAL('currency_exchange_rates_archive_sequence') AS id;
                """)
        ) {
            if (!resultSet.next()) {
                throw new SQLException("The entity id creation error.");
            }

            id = resultSet.getLong("id");
        }

        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        INSERT INTO currency_exchange_rates_archive (
                            id,
                            currency_exchange_rate_id,
                            unit,
                            buy_rate,
                            sale_rate,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            ?, ?, ?, ?, ?, ?, ?
                        );
                """)
        ) {
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, Objects.requireNonNull(currencyExchangeRateArchiveEntity.getCurrencyExchangeRateId()));
            preparedStatement.setInt(3, Objects.requireNonNull(currencyExchangeRateArchiveEntity.getUnit()));
            preparedStatement.setDouble(4, Objects.requireNonNull(currencyExchangeRateArchiveEntity.getBuyRate()));
            preparedStatement.setDouble(5, Objects.requireNonNull(currencyExchangeRateArchiveEntity.getSaleRate()));
            preparedStatement.setTimestamp(6, Timestamp.valueOf(Objects.requireNonNull(currencyExchangeRateArchiveEntity.getCreatedAt())));
            preparedStatement.setTimestamp(7, Timestamp.valueOf(Objects.requireNonNull(currencyExchangeRateArchiveEntity.getUpdatedAt())));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("The entity creation error.");
            }

            currencyExchangeRateArchiveEntity.setId(id);
        }
    }

    @Override
    public Optional<CurrencyExchangeRateArchiveEntity> read(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws SQLException, NullPointerException
    {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        SELECT cera.id,
                        	cera.unit,
                        	cera.buy_rate,
                        	cera.sale_rate,
                        	cera.created_at,
                        	cera.updated_at
                        FROM currency_exchange_rates_archive cera
                        INNER JOIN currency_exchange_rates cer ON (
                            cera.currency_exchange_rate_id = cer.id
                        )
                        WHERE cera.currency_exchange_rate_id = ?
                        ORDER BY updated_at DESC
                        LIMIT 1;
                """)
        ) {
            preparedStatement.setInt(1, Objects.requireNonNull(currencyExchangeRateArchiveEntity.getCurrencyExchangeRateId()));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(
                                new CurrencyExchangeRateArchiveEntity(
                                        resultSet.getLong("id"),
                                        currencyExchangeRateArchiveEntity.getCurrencyExchangeRateId(),
                                        resultSet.getInt("unit"),
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
    public void update(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws SQLException, NullPointerException
    {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        UPDATE currency_exchange_rates_archive
                        SET updated_at = ?
                        WHERE id = ?;
                """)
        ) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(Objects.requireNonNull(currencyExchangeRateArchiveEntity.getUpdatedAt())));
            preparedStatement.setLong(2, Objects.requireNonNull(currencyExchangeRateArchiveEntity.getId()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("The entity updating error.");
            }
        }
    }
}