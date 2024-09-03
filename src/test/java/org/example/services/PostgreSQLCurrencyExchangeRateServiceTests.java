package org.example.services;

import org.apache.commons.dbcp2.BasicDataSource;
import org.example.dao.CurrencyExchangeRateArchiveDAO;
import org.example.dao.PostgreSQLCurrencyExchangeRateArchiveDAO;
import org.example.dao.PostgreSQLCurrencyExchangeRateDAO;
import org.example.dto.CurrencyExchangeRateArchiveDTO;
import org.example.entities.CurrencyExchangeRateArchiveEntity;
import org.example.entities.CurrencyExchangeRateEntity;
import org.example.entities.ScraperEntity;
import org.example.migrators.PostgreSQLMigrator;
import org.example.utils.Config;
import org.example.dto.CurrencyExchangeRateDTO;
import org.example.dto.ScraperDTO;
import org.example.dao.CurrencyExchangeRateDAO;
import org.example.migrators.Migrator;
import org.example.utils.DBCPDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PostgreSQLCurrencyExchangeRateServiceTests {
    private static MockedStatic<Config> mockedStaticConfig;

    private static Migrator migrator;

    private static CurrencyExchangeRateDAO currencyExchangeRateDAO;

    private static CurrencyExchangeRateArchiveDAO currencyExchangeRateArchiveDAO;

    private static CurrencyExchangeRateService currencyExchangeRateService;

    private static BasicDataSource dataSource;

    @BeforeAll
    public static void initAll() throws Exception {
        PostgreSQLCurrencyExchangeRateServiceTests.mockedStaticConfig = Mockito.mockStatic(Config.class);

        // Replacing database configuration properties.
        PostgreSQLCurrencyExchangeRateServiceTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.url"))
                .thenReturn("jdbc:h2:mem:postgres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        PostgreSQLCurrencyExchangeRateServiceTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.username"))
                .thenReturn("sa");
        PostgreSQLCurrencyExchangeRateServiceTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.password"))
                .thenReturn("");

        PostgreSQLCurrencyExchangeRateServiceTests.migrator = new PostgreSQLMigrator();
        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateDAO = new PostgreSQLCurrencyExchangeRateDAO();
        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateArchiveDAO = new PostgreSQLCurrencyExchangeRateArchiveDAO();

        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateService = new CurrencyExchangeRateService(
                PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateDAO,
                PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateArchiveDAO
        );

        PostgreSQLCurrencyExchangeRateServiceTests.dataSource = DBCPDataSource.getDataSource();

        try (Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection()) {
            try {
                // Creating tables of the database.
                PostgreSQLCurrencyExchangeRateServiceTests.migrator.up(connection);

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        try (Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection()) {
            try {
                // Dropping of the database tables.
                PostgreSQLCurrencyExchangeRateServiceTests.migrator.down(connection);

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        } finally {
            PostgreSQLCurrencyExchangeRateServiceTests.dataSource.close();
            PostgreSQLCurrencyExchangeRateServiceTests.mockedStaticConfig.close();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection()) {
            try (
                    PreparedStatement preparedStatement =
                            connection.prepareStatement("""
                                    DELETE FROM currency_exchange_rates_archive;
                                    DELETE FROM currency_exchange_rates;
                                    DELETE FROM scrapers;
                            """)
            ) {
                try {
                    // Clearing the database tables after each test.
                    preparedStatement.executeUpdate();

                    connection.commit();
                } catch (Exception e) {
                    connection.rollback();

                    throw e;
                }
            }
        }
    }

    @Test
    public void saveTest() throws Exception {
        ScraperDTO fisrtScraperDTO = new ScraperDTO(
                PostgreSQLCurrencyExchangeRateServiceTests.FirstScraper.class,
                "Test",
                "Test"
        );
        ScraperDTO secondScraperDTO = new ScraperDTO(
                PostgreSQLCurrencyExchangeRateServiceTests.SecondScraper.class,
                "Test",
                "Test"
        );

        // Seeding the database.
        this.saveScraperDTO(fisrtScraperDTO);
        this.saveScraperDTO(secondScraperDTO);

        /* --------------------------------------------------------------------------------------------------
         * Business Logic Section 1:
         * Adding new entities into the database.
         * --------------------------------------------------------------------------------------------------
         */
        CurrencyExchangeRateDTO firstCurrencyExchangeRateDTO = new CurrencyExchangeRateDTO(
                fisrtScraperDTO.getId(),
                1,
                "USD",
                "UAH",
                2.0,
                3.0
        );

        CurrencyExchangeRateDTO secondCurrencyExchangeRateDTO = new CurrencyExchangeRateDTO(
                secondScraperDTO.getId(),
                1,
                "USD",
                "UAH",
                2.0,
                3.0
        );

        // List of expected DTOs.
        List<CurrencyExchangeRateDTO> expectedCurrencyExchangeRateDTOs =
                Arrays.stream(
                                new CurrencyExchangeRateDTO[]{
                                        // Used first scraper identifier.
                                        firstCurrencyExchangeRateDTO,
                                        // Used second scraper identifier.
                                        secondCurrencyExchangeRateDTO,
                                        // Used another unit currency code.
                                        new CurrencyExchangeRateDTO(
                                                fisrtScraperDTO.getId(),
                                                1,
                                                "EUR",
                                                "UAH",
                                                2.0,
                                                3.0
                                        ),
                                        // Used another rate currency code.
                                        new CurrencyExchangeRateDTO(
                                                fisrtScraperDTO.getId(),
                                                1,
                                                "USD",
                                                "EUR",
                                                2.0,
                                                3.0
                                        )
                                }
                        )
                        .toList();

        // Saving DTOs, using the service.
        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateService.save(
                PostgreSQLCurrencyExchangeRateServiceTests.dataSource,
                expectedCurrencyExchangeRateDTOs
                        .toArray(new CurrencyExchangeRateDTO[expectedCurrencyExchangeRateDTOs.size()])
        );

        // Fetching the entities from the database.
        List<CurrencyExchangeRateEntity> actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities();

        // Checking if database holds expected number of entities.
        assertEquals(expectedCurrencyExchangeRateDTOs.size(), actualCurrencyExchangeRateEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                // Sorting DTOs by date.
                expectedCurrencyExchangeRateDTOs
                        .stream()
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateDTO) o).getUpdatedAt()))
                        .toList(),
                // Mapping entities into DTOs, and sorting them by date.
                actualCurrencyExchangeRateEntities
                        .stream()
                        .map(CurrencyExchangeRateDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateDTO) o).getUpdatedAt()))
                        .toList()
        );

        // Fetching the archive entities from database.
        List<CurrencyExchangeRateArchiveEntity> actualCurrencyExchangeRateArchiveEntities =
                this.readCurrencyExchangeRateArchiveEntities();

        // Checking if database holds expected number of archive entities.
        // Archive entities size should be the same as their entities size,
        // cause all those entities was added into the database, not updated.
        assertEquals(actualCurrencyExchangeRateEntities.size(), actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                // Mapping entities into DTOs, and sorting them by date.
                actualCurrencyExchangeRateEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList(),
                // Mapping entities into DTOs, and sorting them by date.
                actualCurrencyExchangeRateArchiveEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList()
        );

        /* --------------------------------------------------------------------------------------------------
         * Business Logic Section 2:
         * Updating already added entities.
         * Updating the first DTO, the second wasn't changed.
         * --------------------------------------------------------------------------------------------------
         */

        // Updating the date and the unit field of the first DTO.
        firstCurrencyExchangeRateDTO.setUpdatedAt(LocalDateTime.now());
        firstCurrencyExchangeRateDTO.setUnit(1 + firstCurrencyExchangeRateDTO.getUnit());

        // Updating only the date of the second DTO.
        secondCurrencyExchangeRateDTO.setUpdatedAt(LocalDateTime.now());

        // Updating the expected DTOs.
        expectedCurrencyExchangeRateDTOs = Arrays.stream(
                new CurrencyExchangeRateDTO[]{firstCurrencyExchangeRateDTO, secondCurrencyExchangeRateDTO}
        ).toList();

        // Saving DTOs, using the service.
        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateService.save(
                PostgreSQLCurrencyExchangeRateServiceTests.dataSource,
                expectedCurrencyExchangeRateDTOs
                        .toArray(new CurrencyExchangeRateDTO[expectedCurrencyExchangeRateDTOs.size()])
        );

        // Fetching the first DTO entity from the database.
        actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities(firstCurrencyExchangeRateDTO);

        // Checking if database holds that DTO.
        assertFalse(actualCurrencyExchangeRateEntities.isEmpty());

        // Checking if their DTOs are equal.
        assertEquals(
                firstCurrencyExchangeRateDTO,
                new CurrencyExchangeRateDTO(actualCurrencyExchangeRateEntities.getFirst())
        );

        // For the first DTO, fetching the archive entities from database.
        actualCurrencyExchangeRateArchiveEntities = this.readCurrencyExchangeRateArchiveEntities(firstCurrencyExchangeRateDTO);

        // Checking if the database holds expected number of the archive entities.
        // Archive entities size should be equal to 2, cause entity was changed,
        // and the new archive entity should be added into the database.
        assertEquals(2, actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateEntities.getFirst()),
                // Mapping entities into DTOs, sorting them by date and fetch the last one.
                actualCurrencyExchangeRateArchiveEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList()
                        .getLast()
        );

        // Fetching the second DTO entity from the database.
        actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities(secondCurrencyExchangeRateDTO);

        // Checking if the database holds that DTO.
        assertFalse(actualCurrencyExchangeRateEntities.isEmpty());

        // Checking if their DTOs are equal.
        assertEquals(
                secondCurrencyExchangeRateDTO,
                new CurrencyExchangeRateDTO(actualCurrencyExchangeRateEntities.getFirst())
        );

        // For the second DTO, fetching the archive entities from database.
        actualCurrencyExchangeRateArchiveEntities = this.readCurrencyExchangeRateArchiveEntities(secondCurrencyExchangeRateDTO);

        // Checking if database holds expected number of archive entities.
        // Archive entities size should be equal to 1, cause entity wasn't changed,
        // and the current archive entity should be updated.
        assertEquals(1, actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateEntities.getFirst()),
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateArchiveEntities.getFirst())
        );

        /* --------------------------------------------------------------------------------------------------
         * Business Logic Section 3:
         * Updating already added entities.
         * Updating the second DTO, the first wasn't changed.
         * --------------------------------------------------------------------------------------------------
         */

        // Updating only the date of the first DTO.
        firstCurrencyExchangeRateDTO.setUpdatedAt(LocalDateTime.now());

        // Updating the date and the buy rate field of the second DTO.
        secondCurrencyExchangeRateDTO.setUpdatedAt(LocalDateTime.now());
        secondCurrencyExchangeRateDTO.setBuyRate(2 * secondCurrencyExchangeRateDTO.getBuyRate());

        // Updating expected DTOs.
        expectedCurrencyExchangeRateDTOs = Arrays.stream(
                new CurrencyExchangeRateDTO[]{firstCurrencyExchangeRateDTO, secondCurrencyExchangeRateDTO}
        ).toList();

        // Saving DTOs, using the service.
        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateService.save(
                PostgreSQLCurrencyExchangeRateServiceTests.dataSource,
                expectedCurrencyExchangeRateDTOs
                        .toArray(new CurrencyExchangeRateDTO[expectedCurrencyExchangeRateDTOs.size()])
        );

        // Fetching the first DTO entity from the database.
        actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities(firstCurrencyExchangeRateDTO);

        // Checking if the database holds that DTO.
        assertFalse(actualCurrencyExchangeRateEntities.isEmpty());

        // Checking if their DTOs are equal.
        assertEquals(
                firstCurrencyExchangeRateDTO,
                new CurrencyExchangeRateDTO(actualCurrencyExchangeRateEntities.getFirst())
        );

        // For the first DTO, fetching the archive entities from database.
        actualCurrencyExchangeRateArchiveEntities = this.readCurrencyExchangeRateArchiveEntities(firstCurrencyExchangeRateDTO);

        // Checking if database holds expected number of archive entities.
        // Archive entities size should be equal to 2, cause entity wasn't changed,
        // and the current archive entity should be updated in the database.
        assertEquals(2, actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateEntities.getFirst()),
                // Mapping the entities into DTOs, sorting them by date and fetch the last one.
                actualCurrencyExchangeRateArchiveEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList()
                        .getLast()
        );

        // Fetching the second DTO entity from the database.
        actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities(secondCurrencyExchangeRateDTO);

        // Checking if the database holds that DTO.
        assertFalse(actualCurrencyExchangeRateEntities.isEmpty());

        // Checking if their DTOs are equal.
        assertEquals(
                secondCurrencyExchangeRateDTO,
                new CurrencyExchangeRateDTO(actualCurrencyExchangeRateEntities.getFirst())
        );

        // For the second DTO, fetching the archive entities from database.
        actualCurrencyExchangeRateArchiveEntities = this.readCurrencyExchangeRateArchiveEntities(secondCurrencyExchangeRateDTO);

        // Checking if database holds expected number of archive entities.
        // Archive entities size should be equal to 2, cause entity was changed,
        // and new entity should be added into the database.
        assertEquals(2, actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateEntities.getFirst()),
                // Mapping entities into DTOs, sorting them by date and fetch the last one.
                actualCurrencyExchangeRateArchiveEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList()
                        .getLast()
        );

        /* --------------------------------------------------------------------------------------------------
         * Business Logic Section 4:
         * Updating already added entities.
         * Updating the both DTOs.
         * --------------------------------------------------------------------------------------------------
         */

        // Updating the date, the unit, the buy and the sale rate of the first DTO.
        firstCurrencyExchangeRateDTO.setUpdatedAt(LocalDateTime.now());
        firstCurrencyExchangeRateDTO.setUnit(1 + firstCurrencyExchangeRateDTO.getUnit());
        firstCurrencyExchangeRateDTO.setBuyRate(2 * firstCurrencyExchangeRateDTO.getBuyRate());
        firstCurrencyExchangeRateDTO.setSaleRate(2 * firstCurrencyExchangeRateDTO.getSaleRate());

        // Updating the date and the sale rate field of the second DTO.
        secondCurrencyExchangeRateDTO.setUpdatedAt(LocalDateTime.now());
        secondCurrencyExchangeRateDTO.setSaleRate(2 * secondCurrencyExchangeRateDTO.getSaleRate());

        // Updating the expected DTOs.
        expectedCurrencyExchangeRateDTOs = Arrays.stream(
                new CurrencyExchangeRateDTO[]{firstCurrencyExchangeRateDTO, secondCurrencyExchangeRateDTO}
        ).toList();

        // Saving DTOs, using the service.
        PostgreSQLCurrencyExchangeRateServiceTests.currencyExchangeRateService.save(
                PostgreSQLCurrencyExchangeRateServiceTests.dataSource,
                expectedCurrencyExchangeRateDTOs
                        .toArray(new CurrencyExchangeRateDTO[expectedCurrencyExchangeRateDTOs.size()])
        );

        // Fetching the first DTO entity from the database.
        actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities(firstCurrencyExchangeRateDTO);

        // Checking if database holds that DTO.
        assertFalse(actualCurrencyExchangeRateEntities.isEmpty());

        // Checking if their DTOs are equal.
        assertEquals(
                firstCurrencyExchangeRateDTO,
                new CurrencyExchangeRateDTO(actualCurrencyExchangeRateEntities.getFirst())
        );

        // For the first DTO, fetching the archive entities from database.
        actualCurrencyExchangeRateArchiveEntities = this.readCurrencyExchangeRateArchiveEntities(firstCurrencyExchangeRateDTO);

        // Checking if database holds expected number of archive entities.
        // Archive entities size should be equal to 3, cause entity was changed,
        // and the new archive entity should be added in the database.
        assertEquals(3, actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateEntities.getFirst()),
                // Mapping the entities into DTOs, sorting them by date and fetch the last one.
                actualCurrencyExchangeRateArchiveEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList()
                        .getLast()
        );

        // Fetching the second DTO entity from the database.
        actualCurrencyExchangeRateEntities = this.readCurrencyExchangeRateEntities(secondCurrencyExchangeRateDTO);

        // Checking if database holds that DTO.
        assertFalse(actualCurrencyExchangeRateEntities.isEmpty());

        // Checking if their DTOs are equal.
        assertEquals(
                secondCurrencyExchangeRateDTO,
                new CurrencyExchangeRateDTO(actualCurrencyExchangeRateEntities.getFirst())
        );

        // For the second DTO, fetching the archive entities from database.
        actualCurrencyExchangeRateArchiveEntities = this.readCurrencyExchangeRateArchiveEntities(secondCurrencyExchangeRateDTO);

        // Checking if database holds expected number of archive entities.
        // Archive entities size should be equal to 3, cause entity was changed,
        // and new archive entity should be added into the database.
        assertEquals(3, actualCurrencyExchangeRateArchiveEntities.size());

        // Checking if their DTOs are equal.
        assertEquals(
                new CurrencyExchangeRateArchiveDTO(actualCurrencyExchangeRateEntities.getFirst()),
                // Mapping entities into DTOs, sorting them by date and fetch the last one.
                actualCurrencyExchangeRateArchiveEntities
                        .stream()
                        .map(CurrencyExchangeRateArchiveDTO::new)
                        .sorted(Comparator.comparing((Object o) -> ((CurrencyExchangeRateArchiveDTO) o).getUpdatedAt()))
                        .toList()
                        .getLast()
        );
    }

    private void saveScraperDTO(ScraperDTO scraperDTO) throws Exception {
        try (Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection()) {
            try {
                // Getting the entity unique identifier.
                int id;

                try (
                        ResultSet resultSet = connection.createStatement().executeQuery("""
                                SELECT NEXTVAL('scrapers_sequence') AS id;
                        """)
                ) {
                    if (!resultSet.next()) {
                        throw new SQLException("The entity id creation error.");
                    }

                    id = resultSet.getInt("id");
                }

                // Creating instance on the entity (for testing).
                ScraperEntity scraperEntity = new ScraperEntity(
                        id,
                        scraperDTO.getClazz(),
                        scraperDTO.getNameEn(),
                        scraperDTO.getNameUk(),
                        scraperDTO.getCreatedAt(),
                        scraperDTO.getUpdatedAt()
                );

                // Adding the entity into the database.
                try (
                        PreparedStatement preparedStatement = connection.prepareStatement("""
                                INSERT INTO scrapers (
                                    id,
                                    class_name,
                                    name_en,
                                    name_uk,
                                    created_at,
                                    updated_at
                                ) VALUES (
                                    ?, ?, ?, ?, ?, ?
                                );
                        """)
                ) {
                    preparedStatement.setInt(1, scraperEntity.getId());
                    preparedStatement.setString(2, scraperEntity.getClazz().getName());
                    preparedStatement.setString(3, scraperEntity.getNameEn());
                    preparedStatement.setString(4, scraperEntity.getNameUk());
                    preparedStatement.setTimestamp(5, Timestamp.valueOf(scraperEntity.getCreatedAt()));
                    preparedStatement.setTimestamp(6, Timestamp.valueOf(scraperEntity.getUpdatedAt()));

                    int affectedRows = preparedStatement.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("The entity creation error.");
                    }
                }

                connection.commit();

                scraperDTO.setId(scraperEntity.getId());
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }
    }

    private List<CurrencyExchangeRateEntity> readCurrencyExchangeRateEntities() throws Exception {
        List<CurrencyExchangeRateEntity> result = new ArrayList<>();

        try (
                Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection();
                ResultSet resultSet = connection.createStatement().executeQuery("""
                        SELECT id,
                            scraper_id,
                        	unit,
                        	unit_currency_code,
                        	rate_currency_code,
                        	buy_rate,
                        	sale_rate,
                        	created_at,
                        	updated_at
                        FROM currency_exchange_rates;
                """)
        ) {
            try {
                while (resultSet.next()) {
                    result.add(
                            new CurrencyExchangeRateEntity(
                                    resultSet.getInt("id"),
                                    resultSet.getInt("scraper_id"),
                                    resultSet.getInt("unit"),
                                    resultSet.getString("unit_currency_code"),
                                    resultSet.getString("rate_currency_code"),
                                    resultSet.getDouble("buy_rate"),
                                    resultSet.getDouble("sale_rate"),
                                    resultSet.getTimestamp("created_at").toLocalDateTime(),
                                    resultSet.getTimestamp("updated_at").toLocalDateTime()
                            )
                    );
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }

        return result;
    }

    private List<CurrencyExchangeRateEntity> readCurrencyExchangeRateEntities(CurrencyExchangeRateDTO currencyExchangeRateDTO)
            throws Exception
    {
        List<CurrencyExchangeRateEntity> result = new ArrayList<>();

        try (
                Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection();
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
            try {
                preparedStatement.setInt(1, currencyExchangeRateDTO.getScraperId());
                preparedStatement.setString(2, currencyExchangeRateDTO.getUnitCurrencyCode());
                preparedStatement.setString(3, currencyExchangeRateDTO.getRateCurrencyCode());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(
                                new CurrencyExchangeRateEntity(
                                        resultSet.getInt("id"),
                                        currencyExchangeRateDTO.getScraperId(),
                                        resultSet.getInt("unit"),
                                        currencyExchangeRateDTO.getUnitCurrencyCode(),
                                        currencyExchangeRateDTO.getRateCurrencyCode(),
                                        resultSet.getDouble("buy_rate"),
                                        resultSet.getDouble("sale_rate"),
                                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                                        resultSet.getTimestamp("updated_at").toLocalDateTime()
                                )
                        );
                    }
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }

        return result;
    }

    private List<CurrencyExchangeRateArchiveEntity> readCurrencyExchangeRateArchiveEntities() throws Exception {
        List<CurrencyExchangeRateArchiveEntity> result = new ArrayList<>();

        try (
                Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection();
                ResultSet resultSet = connection.createStatement().executeQuery("""
                        SELECT id,
                            currency_exchange_rate_id,
                        	unit,
                        	buy_rate,
                        	sale_rate,
                        	created_at,
                        	updated_at
                        FROM currency_exchange_rates_archive;
                """)
        ) {
            try {
                while (resultSet.next()) {
                    result.add(
                            new CurrencyExchangeRateArchiveEntity(
                                    resultSet.getLong("id"),
                                    resultSet.getInt("currency_exchange_rate_id"),
                                    resultSet.getInt("unit"),
                                    resultSet.getDouble("buy_rate"),
                                    resultSet.getDouble("sale_rate"),
                                    resultSet.getTimestamp("created_at").toLocalDateTime(),
                                    resultSet.getTimestamp("updated_at").toLocalDateTime()
                            )
                    );
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }

        return result;
    }

    private List<CurrencyExchangeRateArchiveEntity> readCurrencyExchangeRateArchiveEntities(CurrencyExchangeRateDTO currencyExchangeRateDTO)
            throws Exception
    {
        List<CurrencyExchangeRateArchiveEntity> result = new ArrayList<>();

        try (
                Connection connection = PostgreSQLCurrencyExchangeRateServiceTests.dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        SELECT cera.id,
                            cera.currency_exchange_rate_id,
                        	cera.unit,
                        	cera.buy_rate,
                        	cera.sale_rate,
                        	cera.created_at,
                        	cera.updated_at
                        FROM currency_exchange_rates_archive cera
                        INNER JOIN currency_exchange_rates cer ON (
                            cera.currency_exchange_rate_id = cer.id
                        )
                        WHERE cer.scraper_id = ?
                            AND cer.unit_currency_code = ?
                            AND cer.rate_currency_code = ?;
                """)
        ) {
            try {
                preparedStatement.setInt(1, currencyExchangeRateDTO.getScraperId());
                preparedStatement.setString(2, currencyExchangeRateDTO.getUnitCurrencyCode());
                preparedStatement.setString(3, currencyExchangeRateDTO.getRateCurrencyCode());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(
                                new CurrencyExchangeRateArchiveEntity(
                                        resultSet.getLong("id"),
                                        resultSet.getInt("currency_exchange_rate_id"),
                                        resultSet.getInt("unit"),
                                        resultSet.getDouble("buy_rate"),
                                        resultSet.getDouble("sale_rate"),
                                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                                        resultSet.getTimestamp("updated_at").toLocalDateTime()
                                )
                        );
                    }
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }

        return result;
    }

    // The first class for testing.
    private static class FirstScraper extends org.example.scrapers.Scraper {
        public FirstScraper(Integer id) {
            super(id);
        }

        @Override
        protected Object[] getItems(Object root) throws Exception {
            return new Object[0];
        }

        @Override
        protected Integer getUnit(Object item) throws Exception {
            return null;
        }

        @Override
        protected String getUnitCurrencyCode(Object item) throws Exception {
            return null;
        }

        @Override
        protected String getRateCurrencyCode(Object item) throws Exception {
            return null;
        }

        @Override
        protected Double getBuyRate(Object item) throws Exception {
            return null;
        }

        @Override
        protected Double getSaleRate(Object item) throws Exception {
            return null;
        }

        @Override
        protected Predicate<CurrencyExchangeRateDTO> getPredicate() {
            return null;
        }

        @Override
        protected URI getURI() throws URISyntaxException {
            return null;
        }

        @Override
        protected Duration getTimeout() {
            return null;
        }

        @Override
        protected String getMethod() {
            return null;
        }

        @Override
        protected HttpRequest.BodyPublisher getBodyPublisher() {
            return null;
        }

        @Override
        protected String[] getHeaders() {
            return new String[0];
        }
    }

    // The second class for testing.
    private static class SecondScraper extends org.example.scrapers.Scraper {
        public SecondScraper(Integer id) {
            super(id);
        }

        @Override
        protected Object[] getItems(Object root) throws Exception {
            return new Object[0];
        }

        @Override
        protected Integer getUnit(Object item) throws Exception {
            return null;
        }

        @Override
        protected String getUnitCurrencyCode(Object item) throws Exception {
            return null;
        }

        @Override
        protected String getRateCurrencyCode(Object item) throws Exception {
            return null;
        }

        @Override
        protected Double getBuyRate(Object item) throws Exception {
            return null;
        }

        @Override
        protected Double getSaleRate(Object item) throws Exception {
            return null;
        }

        @Override
        protected Predicate<CurrencyExchangeRateDTO> getPredicate() {
            return null;
        }

        @Override
        protected URI getURI() throws URISyntaxException {
            return null;
        }

        @Override
        protected Duration getTimeout() {
            return null;
        }

        @Override
        protected String getMethod() {
            return null;
        }

        @Override
        protected HttpRequest.BodyPublisher getBodyPublisher() {
            return null;
        }

        @Override
        protected String[] getHeaders() {
            return new String[0];
        }
    }
}