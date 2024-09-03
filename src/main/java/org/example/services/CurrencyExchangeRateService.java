package org.example.services;

import org.example.dao.CurrencyExchangeRateArchiveDAO;
import org.example.dto.CurrencyExchangeRateDTO;
import org.example.entities.CurrencyExchangeRateArchiveEntity;
import org.example.entities.CurrencyExchangeRateEntity;
import org.example.dao.CurrencyExchangeRateDAO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class CurrencyExchangeRateService {
    private final CurrencyExchangeRateDAO currencyExchangeRateDAO;

    private final CurrencyExchangeRateArchiveDAO currencyExchangeRateArchiveDAO;

    /**
     * Constructor.
     * @param currencyExchangeRateDAO Instance of the DAO.
     * @param currencyExchangeRateArchiveDAO Instance of the archive DAO.
     */
    public CurrencyExchangeRateService(
            CurrencyExchangeRateDAO currencyExchangeRateDAO,
            CurrencyExchangeRateArchiveDAO currencyExchangeRateArchiveDAO
    ) {
        this.currencyExchangeRateDAO = currencyExchangeRateDAO;
        this.currencyExchangeRateArchiveDAO = currencyExchangeRateArchiveDAO;
    }

    /**
     * Saving DTOs into the database.
     * @param dataSource Instance of the data source.
     * @param currencyExchangeRateDTOs Array of the DTOs.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If one of the required DTO fields is null.
     */
    public void save(DataSource dataSource, CurrencyExchangeRateDTO[] currencyExchangeRateDTOs)
            throws SQLException, NullPointerException
    {
        try (Connection connection = dataSource.getConnection()) {
            try {
                for (CurrencyExchangeRateDTO currencyExchangeRateDTO : currencyExchangeRateDTOs) {
                    // Mapping the DTO into the entity.
                    CurrencyExchangeRateEntity currencyExchangeRateEntity =
                            new CurrencyExchangeRateEntity(currencyExchangeRateDTO);

                    // Searching this entity in the database.
                    Optional<CurrencyExchangeRateEntity> optionalCurrentCurrencyExchangeRateEntity =
                            this.currencyExchangeRateDAO.read(connection, currencyExchangeRateEntity);

                    // The entity is present in the database.
                    // Updating that entity in the database.
                    if (optionalCurrentCurrencyExchangeRateEntity.isPresent()) {
                        CurrencyExchangeRateEntity currentCurrencyExchangeRateEntity =
                                optionalCurrentCurrencyExchangeRateEntity.get();

                        currentCurrencyExchangeRateEntity.setUnit(currencyExchangeRateEntity.getUnit());
                        currentCurrencyExchangeRateEntity.setBuyRate(currencyExchangeRateEntity.getBuyRate());
                        currentCurrencyExchangeRateEntity.setSaleRate(currencyExchangeRateEntity.getSaleRate());
                        currentCurrencyExchangeRateEntity.setUpdatedAt(currencyExchangeRateEntity.getUpdatedAt());

                        // Updating that entity.
                        this.currencyExchangeRateDAO.update(connection, currentCurrencyExchangeRateEntity);

                        currencyExchangeRateEntity.setId(currentCurrencyExchangeRateEntity.getId());
                    } else {
                        // The entity is not present in the database.
                        // Adding the entity as the new one.
                        this.currencyExchangeRateDAO.create(connection, currencyExchangeRateEntity);
                    }

                    // Mapping the entity into the archive entity.
                    CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity =
                            new CurrencyExchangeRateArchiveEntity(currencyExchangeRateEntity);

                    // Searching this archive entity in the database.
                    Optional<CurrencyExchangeRateArchiveEntity> optionalCurrentCurrencyExchangeRateArchiveEntity =
                            this.currencyExchangeRateArchiveDAO.read(connection, currencyExchangeRateArchiveEntity);

                    // The archive entity was found in the database.
                    // Comparing current archive entity with the new one.
                    // If they are the same, then updating current archive entity date,
                    // if they not, then adding the archive entity as the new one.
                    if (optionalCurrentCurrencyExchangeRateArchiveEntity.isPresent()) {
                        CurrencyExchangeRateArchiveEntity currentCurrencyExchangeRateArchiveEntity =
                                optionalCurrentCurrencyExchangeRateArchiveEntity.get();

                        // The current archive entity is the same as the new one.
                        if (
                                currentCurrencyExchangeRateArchiveEntity.getUnit().equals(currencyExchangeRateArchiveEntity.getUnit())
                                        && currentCurrencyExchangeRateArchiveEntity.getBuyRate().equals(currencyExchangeRateArchiveEntity.getBuyRate())
                                        && currentCurrencyExchangeRateArchiveEntity.getSaleRate().equals(currencyExchangeRateArchiveEntity.getSaleRate())
                        ) {
                            // Syncing the new archive entity with the current one.
                            currencyExchangeRateArchiveEntity.setId(currentCurrencyExchangeRateArchiveEntity.getId());
                            currencyExchangeRateArchiveEntity.setCreatedAt(currentCurrencyExchangeRateArchiveEntity.getCreatedAt());

                            // Updating that archive entity.
                            this.currencyExchangeRateArchiveDAO.update(connection, currencyExchangeRateArchiveEntity);
                        } else {
                            // The current archive entity is different that the new one.
                            // Adding the archive entity as the new one.
                            this.currencyExchangeRateArchiveDAO.create(connection, currencyExchangeRateArchiveEntity);
                        }
                    } else {
                        // The archive entity wasn't found in the database.
                        // Adding the archive entity as the new one.
                        this.currencyExchangeRateArchiveDAO.create(connection, currencyExchangeRateArchiveEntity);
                    }
                }

                connection.commit();
            } catch (SQLException | NullPointerException e) {
                connection.rollback();

                throw e;
            }
        }
    }
}