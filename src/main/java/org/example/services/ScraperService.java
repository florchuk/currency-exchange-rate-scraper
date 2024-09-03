package org.example.services;

import org.example.dto.ScraperDTO;
import org.example.dao.ScraperDAO;
import org.example.entities.ScraperEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class ScraperService {
    private final ScraperDAO scraperDAO;

    /**
     * Constructor.
     * @param scraperDAO Instance of the DAO.
     */
    public ScraperService(ScraperDAO scraperDAO) {
        this.scraperDAO = scraperDAO;
    }

    /**
     * Reading the DTO out of the database.
     * @param dataSource Instance of the data source.
     * @param scraperDTO Instance of the DTO, that will be used for search.
     * @return The DTO from the database.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If one of the required DTO fields is null, or if the entity wasn't found in the database.
     */
    public ScraperDTO read(DataSource dataSource, ScraperDTO scraperDTO)
            throws SQLException, NullPointerException
    {
        try (Connection connection = dataSource.getConnection()) {
            try {
                Optional<ScraperEntity> optionalScraperEntity =
                        this.scraperDAO.read(connection, new ScraperEntity(scraperDTO));

                if (optionalScraperEntity.isEmpty()) {
                    throw new NullPointerException(
                            String.format("\"%s\" was not found in the database.", scraperDTO.getClazz().getName())
                    );
                }

                connection.commit();

                return new ScraperDTO(optionalScraperEntity.get());
            } catch (SQLException | NullPointerException e) {
                connection.rollback();

                throw e;
            }
        }
    }
}