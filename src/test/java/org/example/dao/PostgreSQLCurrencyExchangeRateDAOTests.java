package org.example.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.example.entities.ScraperEntity;
import org.example.scrapers.Scraper;
import org.example.utils.Config;
import org.example.utils.DBCPDataSource;
import org.example.dto.CurrencyExchangeRateDTO;
import org.example.entities.CurrencyExchangeRateEntity;
import org.example.migrators.Migrator;
import org.example.migrators.PostgreSQLMigrator;
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

public class PostgreSQLCurrencyExchangeRateDAOTests {
    private static MockedStatic<Config> mockedStaticConfig;

    private static Migrator migrator;

    private static CurrencyExchangeRateDAO currencyExchangeRateDAO;

    private static BasicDataSource dataSource;

    private static Connection connection;

    @BeforeAll
    public static void initAll() throws Exception {
        PostgreSQLCurrencyExchangeRateDAOTests.mockedStaticConfig = Mockito.mockStatic(Config.class);

        // Replacing database configuration properties.
        PostgreSQLCurrencyExchangeRateDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.url"))
                .thenReturn("jdbc:h2:mem:postgres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        PostgreSQLCurrencyExchangeRateDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.username"))
                .thenReturn("sa");
        PostgreSQLCurrencyExchangeRateDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.password"))
                .thenReturn("");

        PostgreSQLCurrencyExchangeRateDAOTests.migrator = new PostgreSQLMigrator();
        PostgreSQLCurrencyExchangeRateDAOTests.currencyExchangeRateDAO = new PostgreSQLCurrencyExchangeRateDAO();
        PostgreSQLCurrencyExchangeRateDAOTests.dataSource = DBCPDataSource.getDataSource();
        PostgreSQLCurrencyExchangeRateDAOTests.connection =
                PostgreSQLCurrencyExchangeRateDAOTests.dataSource.getConnection();

        try {
            // Creating tables of the database.
            PostgreSQLCurrencyExchangeRateDAOTests.migrator.up(PostgreSQLCurrencyExchangeRateDAOTests.connection);

            PostgreSQLCurrencyExchangeRateDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateDAOTests.connection.rollback();

            throw e;
        }
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        try {
            // Dropping of the database tables.
            PostgreSQLCurrencyExchangeRateDAOTests.migrator
                    .down(PostgreSQLCurrencyExchangeRateDAOTests.connection);

            PostgreSQLCurrencyExchangeRateDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateDAOTests.connection.rollback();

            throw e;
        } finally {
            PostgreSQLCurrencyExchangeRateDAOTests.connection.close();
            PostgreSQLCurrencyExchangeRateDAOTests.dataSource.close();
            PostgreSQLCurrencyExchangeRateDAOTests.mockedStaticConfig.close();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (
                PreparedStatement preparedStatement =
                        PostgreSQLCurrencyExchangeRateDAOTests.connection.prepareStatement("""
                                DELETE FROM currency_exchange_rates;
                                DELETE FROM scrapers;
                        """)
        ) {
            try {
                // Clearing the database tables after each test.
                preparedStatement.executeUpdate();

                PostgreSQLCurrencyExchangeRateDAOTests.connection.commit();
            } catch (Exception e) {
                PostgreSQLCurrencyExchangeRateDAOTests.connection.rollback();

                throw e;
            }
        }
    }

    @Test
    public void createTest() throws Exception {
        CurrencyExchangeRateEntity expectedCurrencyExchangeRateEntity = new CurrencyExchangeRateEntity(
                null,
                this.getScraperEntity().getId(),
                1,
                "UAH",
                "USD",
                2.0,
                3.0
        );

        try {
            // Creating the entity.
            PostgreSQLCurrencyExchangeRateDAOTests.currencyExchangeRateDAO.create(
                    PostgreSQLCurrencyExchangeRateDAOTests.connection,
                    expectedCurrencyExchangeRateEntity
            );

            // Fetching the entity.
            Optional<CurrencyExchangeRateEntity> optionalActualCurrencyExchangeRateEntity = this.read(
                    PostgreSQLCurrencyExchangeRateDAOTests.connection,
                    expectedCurrencyExchangeRateEntity.getId()
            );

            // Checking that the entity is exists, and that entity is equal to expected one.
            assertTrue(optionalActualCurrencyExchangeRateEntity.isPresent());
            assertEquals(expectedCurrencyExchangeRateEntity, optionalActualCurrencyExchangeRateEntity.get());

            PostgreSQLCurrencyExchangeRateDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateDAOTests.connection.rollback();

            throw e;
        }
    }

    @Test
    public void readTest() throws Exception {
        CurrencyExchangeRateEntity expectedCurrencyExchangeRateEntity = new CurrencyExchangeRateEntity(
                null,
                this.getScraperEntity().getId(),
                1,
                "UAH",
                "USD",
                2.0,
                3.0
        );

        try {
            // Creating the entity.
            this.create(PostgreSQLCurrencyExchangeRateDAOTests.connection, expectedCurrencyExchangeRateEntity);

            // Reading the entity.
            Optional<CurrencyExchangeRateEntity> optionalActualCurrencyExchangeRateEntity =
                    PostgreSQLCurrencyExchangeRateDAOTests.currencyExchangeRateDAO.read(
                            PostgreSQLCurrencyExchangeRateDAOTests.connection,
                            expectedCurrencyExchangeRateEntity
                    );

            // Checking that the entity is exists, and that entity is equal to expected one.
            assertTrue(optionalActualCurrencyExchangeRateEntity.isPresent());
            assertEquals(expectedCurrencyExchangeRateEntity, optionalActualCurrencyExchangeRateEntity.get());

            PostgreSQLCurrencyExchangeRateDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateDAOTests.connection.rollback();

            throw e;
        }
    }

    @Test
    public void updateTest() throws Exception {
        CurrencyExchangeRateEntity originalCurrencyExchangeRateEntity = new CurrencyExchangeRateEntity(
                null,
                this.getScraperEntity().getId(),
                1,
                "UAH",
                "USD",
                2.0,
                3.0
        );

        try {
            // Creating the entity.
            this.create(PostgreSQLCurrencyExchangeRateDAOTests.connection, originalCurrencyExchangeRateEntity);

            Optional<CurrencyExchangeRateEntity> optionalOriginalCurrencyExchangeRateEntity = this.read(
                    PostgreSQLCurrencyExchangeRateDAOTests.connection,
                    originalCurrencyExchangeRateEntity.getId()
            );

            // Checking that the entity is exists, and that entity is equal to original one.
            assertTrue(optionalOriginalCurrencyExchangeRateEntity.isPresent());
            assertEquals(
                    originalCurrencyExchangeRateEntity,
                    optionalOriginalCurrencyExchangeRateEntity.get()
            );

            // Cloning original entity, and updating some fields in it.
            CurrencyExchangeRateEntity expectedCurrencyExchangeRateEntity = new CurrencyExchangeRateEntity(
                    originalCurrencyExchangeRateEntity.getId(),
                    originalCurrencyExchangeRateEntity.getScraperId(),
                    originalCurrencyExchangeRateEntity.getUnit() * 2,
                    originalCurrencyExchangeRateEntity.getUnitCurrencyCode(),
                    originalCurrencyExchangeRateEntity.getRateCurrencyCode(),
                    originalCurrencyExchangeRateEntity.getBuyRate() * 2,
                    originalCurrencyExchangeRateEntity.getSaleRate() * 2,
                    originalCurrencyExchangeRateEntity.getCreatedAt(),
                    originalCurrencyExchangeRateEntity.getUpdatedAt().plusSeconds(1)
            );

            // Checking that original and expected entities are not equal.
            assertNotEquals(originalCurrencyExchangeRateEntity, expectedCurrencyExchangeRateEntity);

            // Updating the entity.
            PostgreSQLCurrencyExchangeRateDAOTests.currencyExchangeRateDAO.update(
                    PostgreSQLCurrencyExchangeRateDAOTests.connection,
                    expectedCurrencyExchangeRateEntity
            );

            // Fetching the updated entity.
            Optional<CurrencyExchangeRateEntity> optionalExpectedCurrencyExchangeRateEntity = this.read(
                    PostgreSQLCurrencyExchangeRateDAOTests.connection,
                    expectedCurrencyExchangeRateEntity.getId()
            );

            // Checking that the updated entity exist, and that entity is equal to the expected one.
            assertTrue(optionalExpectedCurrencyExchangeRateEntity.isPresent());
            assertEquals(expectedCurrencyExchangeRateEntity, optionalExpectedCurrencyExchangeRateEntity.get());

            PostgreSQLCurrencyExchangeRateDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLCurrencyExchangeRateDAOTests.connection.rollback();

            throw e;
        }
    }

    private void create(Connection connection, CurrencyExchangeRateEntity currencyExchangeRateEntity) throws Exception {
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
        }
    }

    private Optional<CurrencyExchangeRateEntity> read(Connection connection, Integer id) throws Exception {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("""
                        SELECT scraper_id,
                            unit,
                            unit_currency_code,
                            rate_currency_code,
                            buy_rate,
                            sale_rate,
                            created_at,
                            updated_at
                        FROM currency_exchange_rates
                        WHERE id = ?;
                """)
        ) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(
                            new CurrencyExchangeRateEntity(
                                    id,
                                    resultSet.getInt("scraper_id"),
                                    resultSet.getInt("unit"),
                                    resultSet.getString("unit_currency_code"),
                                    resultSet.getString("rate_currency_code"),
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

    private ScraperEntity getScraperEntity() throws Exception {
        // Getting the entity unique identifier.
        int id;

        try (
                ResultSet resultSet = PostgreSQLCurrencyExchangeRateDAOTests.connection.createStatement().executeQuery("""
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
                PostgreSQLCurrencyExchangeRateDAOTests.TestScraper.class,
                "Test",
                "Тест"
        );

        // Adding the entity into the database.
        try (
                PreparedStatement preparedStatement =
                        PostgreSQLCurrencyExchangeRateDAOTests.connection.prepareStatement("""
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