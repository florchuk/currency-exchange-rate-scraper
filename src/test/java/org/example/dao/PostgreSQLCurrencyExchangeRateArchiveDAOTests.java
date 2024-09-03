package org.example.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.example.dto.CurrencyExchangeRateDTO;
import org.example.entities.CurrencyExchangeRateArchiveEntity;
import org.example.entities.CurrencyExchangeRateEntity;
import org.example.entities.ScraperEntity;
import org.example.migrators.Migrator;
import org.example.migrators.PostgreSQLMigrator;
import org.example.scrapers.Scraper;
import org.example.utils.Config;
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
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class PostgreSQLCurrencyExchangeRateArchiveDAOTests {
    private static MockedStatic<Config> mockedStaticConfig;

    private static Migrator migrator;

    private static CurrencyExchangeRateDAO currencyExchangeRateDAO;

    private static CurrencyExchangeRateArchiveDAO currencyExchangeRateArchiveDAO;

    private static BasicDataSource dataSource;

    private static Connection connection;

    @BeforeAll
    public static void initAll() throws Exception {
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.mockedStaticConfig = Mockito.mockStatic(Config.class);

        // Replacing database configuration properties.
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.url"))
                .thenReturn("jdbc:h2:mem:postgres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.username"))
                .thenReturn("sa");
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.password"))
                .thenReturn("");

        PostgreSQLCurrencyExchangeRateArchiveDAOTests.migrator = new PostgreSQLMigrator();
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.currencyExchangeRateDAO = new PostgreSQLCurrencyExchangeRateDAO();
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.currencyExchangeRateArchiveDAO =
                new PostgreSQLCurrencyExchangeRateArchiveDAO();
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.dataSource = DBCPDataSource.getDataSource();
        PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection =
                PostgreSQLCurrencyExchangeRateArchiveDAOTests.dataSource.getConnection();

        try {
            // Creating tables of the database.
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.migrator
                    .up(PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection);

            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.rollback();

            throw e;
        }
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        try {
            // Dropping of the database tables.
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.migrator
                    .down(PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection);

            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.rollback();

            throw e;
        } finally {
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.close();
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.dataSource.close();
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.mockedStaticConfig.close();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (
                PreparedStatement preparedStatement =
                        PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.prepareStatement("""
                                DELETE FROM currency_exchange_rates_archive;
                                DELETE FROM currency_exchange_rates;
                                DELETE FROM scrapers;
                        """)
        ) {
            try {
                // Clearing the database tables after each test.
                preparedStatement.executeUpdate();

                PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.commit();
            } catch (Exception e) {
                PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.rollback();

                throw e;
            }
        }
    }

    @Test
    public void createTest() throws Exception {
        CurrencyExchangeRateArchiveEntity expectedCurrencyExchangeRateArchiveEntity =
                new CurrencyExchangeRateArchiveEntity(this.getCurrencyExchangeRateEntity());

        try {
            // Creating the entity.
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.currencyExchangeRateArchiveDAO.create(
                    PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection,
                    expectedCurrencyExchangeRateArchiveEntity
            );

            // Fetching the entity.
            Optional<CurrencyExchangeRateArchiveEntity> optionalActualCurrencyExchangeRateArchiveEntity = this.read(
                    PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection,
                    expectedCurrencyExchangeRateArchiveEntity.getId()
            );

            // Checking that the entity is exists, and that entity is equal to expected one.
            assertTrue(optionalActualCurrencyExchangeRateArchiveEntity.isPresent());
            assertEquals(
                    expectedCurrencyExchangeRateArchiveEntity,
                    optionalActualCurrencyExchangeRateArchiveEntity.get()
            );

            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.rollback();

            throw e;
        }
    }

    @Test
    public void readTest() throws Exception {
        CurrencyExchangeRateArchiveEntity expectedCurrencyExchangeRateArchiveEntity =
                new CurrencyExchangeRateArchiveEntity(this.getCurrencyExchangeRateEntity());

        try {
            // Creating the entity.
            this.create(
                    PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection,
                    expectedCurrencyExchangeRateArchiveEntity
            );

            // Fetching the entity.
            Optional<CurrencyExchangeRateArchiveEntity> optionalActualCurrencyExchangeRateArchiveEntity =
                    PostgreSQLCurrencyExchangeRateArchiveDAOTests.currencyExchangeRateArchiveDAO.read(
                            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection,
                            expectedCurrencyExchangeRateArchiveEntity
                    );

            // Checking that the entity is exists, and that entity is equal to expected one.
            assertTrue(optionalActualCurrencyExchangeRateArchiveEntity.isPresent());
            assertEquals(expectedCurrencyExchangeRateArchiveEntity, optionalActualCurrencyExchangeRateArchiveEntity.get());

            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.rollback();

            throw e;
        }
    }

    @Test
    public void updateTest() throws Exception {
        CurrencyExchangeRateArchiveEntity originalCurrencyExchangeRateArchiveEntity =
                new CurrencyExchangeRateArchiveEntity(this.getCurrencyExchangeRateEntity());

        try {
            // Creating the entity.
            this.create(PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection, originalCurrencyExchangeRateArchiveEntity);

            // Cloning original entity, and updating some fields in it.
            CurrencyExchangeRateArchiveEntity expectedCurrencyExchangeRateArchiveEntity = new CurrencyExchangeRateArchiveEntity(
                    originalCurrencyExchangeRateArchiveEntity.getId(),
                    originalCurrencyExchangeRateArchiveEntity.getCurrencyExchangeRateId(),
                    originalCurrencyExchangeRateArchiveEntity.getUnit(),
                    originalCurrencyExchangeRateArchiveEntity.getBuyRate(),
                    originalCurrencyExchangeRateArchiveEntity.getSaleRate(),
                    originalCurrencyExchangeRateArchiveEntity.getCreatedAt(),
                    originalCurrencyExchangeRateArchiveEntity.getUpdatedAt().plusSeconds(1)
            );

            // Checking that original and expected entities are not equal.
            assertNotEquals(originalCurrencyExchangeRateArchiveEntity, expectedCurrencyExchangeRateArchiveEntity);

            // Updating the entity.
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.currencyExchangeRateArchiveDAO.update(
                    PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection,
                    expectedCurrencyExchangeRateArchiveEntity
            );

            // Fetching the updated entity.
            Optional<CurrencyExchangeRateArchiveEntity> optionalExpectedCurrencyExchangeRateArchiveEntity = this.read(
                    PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection,
                    expectedCurrencyExchangeRateArchiveEntity.getId()
            );

            // Checking that the updated entity exist, and that entity is equal to the expected one.
            assertTrue(optionalExpectedCurrencyExchangeRateArchiveEntity.isPresent());
            assertEquals(
                    expectedCurrencyExchangeRateArchiveEntity,
                    optionalExpectedCurrencyExchangeRateArchiveEntity.get()
            );

            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.rollback();

            throw e;
        }
    }

    private void create(Connection connection, CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity)
            throws Exception
    {
        long id;

        try (
                ResultSet resultSet = connection.createStatement().executeQuery("""
                        SELECT NEXTVAL('currency_exchange_rates_archive_sequence') AS id;
                """)
        ) {
            if (!resultSet.next()) {
                throw new SQLException("The entity id can't be created.");
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
                        ) VALUES (
                            ?, ?, ?, ?, ?, ?, ?
                        );
                """)
        ) {
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, currencyExchangeRateArchiveEntity.getCurrencyExchangeRateId());
            preparedStatement.setInt(3, currencyExchangeRateArchiveEntity.getUnit());
            preparedStatement.setDouble(4, currencyExchangeRateArchiveEntity.getBuyRate());
            preparedStatement.setDouble(5, currencyExchangeRateArchiveEntity.getSaleRate());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(currencyExchangeRateArchiveEntity.getCreatedAt()));
            preparedStatement.setTimestamp(7, Timestamp.valueOf(currencyExchangeRateArchiveEntity.getUpdatedAt()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("The entity creation error.");
            }

            currencyExchangeRateArchiveEntity.setId(id);
        }
    }

    private Optional<CurrencyExchangeRateArchiveEntity> read(Connection connection, Long id) throws Exception {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        SELECT currency_exchange_rate_id,
                            unit,
                            buy_rate,
                            sale_rate,
                            created_at,
                            updated_at
                        FROM currency_exchange_rates_archive
                        WHERE id = ?;
                """)
        ) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(
                            new CurrencyExchangeRateArchiveEntity(
                                    id,
                                    resultSet.getInt("currency_exchange_rate_id"),
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

    private CurrencyExchangeRateEntity getCurrencyExchangeRateEntity() throws Exception {
        CurrencyExchangeRateEntity currencyExchangeRateEntity = new CurrencyExchangeRateEntity(
                new CurrencyExchangeRateDTO(
                        this.getScraperEntity().getId(),
                        1,
                        "UAH",
                        "USD",
                        2.0,
                        3.0
                )
        );

        int id;

        try (
                ResultSet resultSet = connection.createStatement().executeQuery("""
                        SELECT NEXTVAL('currency_exchange_rates_sequence') AS id;
                """)
        ) {
            if (!resultSet.next()) {
                throw new SQLException("The entity id can't be created.");
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
                        ) VALUES (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?
                        );
                """)
        ) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, currencyExchangeRateEntity.getScraperId());
            preparedStatement.setInt(3, currencyExchangeRateEntity.getUnit());
            preparedStatement.setString(4, currencyExchangeRateEntity.getUnitCurrencyCode());
            preparedStatement.setString(5, currencyExchangeRateEntity.getRateCurrencyCode());
            preparedStatement.setDouble(6, currencyExchangeRateEntity.getBuyRate());
            preparedStatement.setDouble(7, currencyExchangeRateEntity.getSaleRate());
            preparedStatement.setTimestamp(8, Timestamp.valueOf(currencyExchangeRateEntity.getCreatedAt()));
            preparedStatement.setTimestamp(9, Timestamp.valueOf(currencyExchangeRateEntity.getUpdatedAt()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("The entity creation error.");
            }

            currencyExchangeRateEntity.setId(id);

            return currencyExchangeRateEntity;
        }
    }

    private ScraperEntity getScraperEntity() throws Exception {
        // Getting the entity unique identifier.
        int id;

        try (
                ResultSet resultSet = PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.createStatement().executeQuery("""
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
                PostgreSQLCurrencyExchangeRateArchiveDAOTests.TestScraper.class,
                "Test",
                "Тест"
        );

        // Adding the entity into the database.
        try (
                PreparedStatement preparedStatement =
                        PostgreSQLCurrencyExchangeRateArchiveDAOTests.connection.prepareStatement("""
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

        return scraperEntity;
    }

    // The class for testing.
    private static class TestScraper extends Scraper {
        public TestScraper(Integer id) {
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