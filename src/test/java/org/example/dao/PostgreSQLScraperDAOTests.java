package org.example.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.example.entities.ScraperEntity;
import org.example.scrapers.Scraper;
import org.example.utils.Config;
import org.example.utils.DBCPDataSource;
import org.example.dto.CurrencyExchangeRateDTO;
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

public class PostgreSQLScraperDAOTests {
    private static MockedStatic<Config> mockedStaticConfig;

    private static Migrator migrator;

    private static ScraperDAO scraperDAO;

    private static BasicDataSource dataSource;

    private static Connection connection;

    @BeforeAll
    public static void initAll() throws Exception {
        PostgreSQLScraperDAOTests.mockedStaticConfig = Mockito.mockStatic(Config.class);

        // Replacing database configuration properties.
        PostgreSQLScraperDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.url"))
                .thenReturn("jdbc:h2:mem:postgres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        PostgreSQLScraperDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.username"))
                .thenReturn("sa");
        PostgreSQLScraperDAOTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.password"))
                .thenReturn("");

        PostgreSQLScraperDAOTests.migrator = new PostgreSQLMigrator();
        PostgreSQLScraperDAOTests.scraperDAO = new PostgreSQLScraperDAO();
        PostgreSQLScraperDAOTests.dataSource = DBCPDataSource.getDataSource();
        PostgreSQLScraperDAOTests.connection = PostgreSQLScraperDAOTests.dataSource.getConnection();

        try {
            // Creating tables of the database.
            PostgreSQLScraperDAOTests.migrator.up(PostgreSQLScraperDAOTests.connection);

            PostgreSQLScraperDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLScraperDAOTests.connection.rollback();

            throw e;
        }
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        try {
            // Dropping of the database tables.
            PostgreSQLScraperDAOTests.migrator.down(PostgreSQLScraperDAOTests.connection);

            PostgreSQLScraperDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLScraperDAOTests.connection.rollback();

            throw e;
        } finally {
            PostgreSQLScraperDAOTests.connection.close();
            PostgreSQLScraperDAOTests.dataSource.close();
            PostgreSQLScraperDAOTests.mockedStaticConfig.close();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (
                PreparedStatement preparedStatement =
                        PostgreSQLScraperDAOTests.connection.prepareStatement("""
                                DELETE FROM scrapers;
                        """)
        ) {
            try {
                // Clearing the database tables after each test.
                preparedStatement.executeUpdate();

                PostgreSQLScraperDAOTests.connection.commit();
            } catch (Exception e) {
                PostgreSQLScraperDAOTests.connection.rollback();

                throw e;
            }
        }
    }

    @Test
    public void readTest() throws Exception {
        try {
            try (
                    ResultSet resultSet =
                            PostgreSQLScraperDAOTests.connection.createStatement().executeQuery("""
                                    SELECT id,
                                        class_name,
                                        name_en,
                                        name_uk,
                                        created_at,
                                        updated_at
                                    FROM scrapers;
                            """)
            ) {
                // At the beginning, database should be empty.
                assertFalse(resultSet.next());
            }

            // Creating the entity unique identifier.
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
                    PostgreSQLScraperDAOTests.TestScraper.class,
                    "Test",
                    "Тест"
            );

            // Fetching before adding the entity into the database.
            assertTrue(
                    PostgreSQLScraperDAOTests.scraperDAO.read(
                            PostgreSQLScraperDAOTests.connection,
                            scraperEntity
                    ).isEmpty()
            );

            // Adding the entity into the database.
            try (
                    PreparedStatement preparedStatement = PostgreSQLScraperDAOTests.connection.prepareStatement("""
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

            // Reading the entity out of database.
            Optional<ScraperEntity> optionalScraperEntity = PostgreSQLScraperDAOTests.scraperDAO.read(
                    PostgreSQLScraperDAOTests.connection,
                    scraperEntity
            );

            // The entity should be present.
            assertTrue(optionalScraperEntity.isPresent());
            // The instance of the entity should be equal to the entity fetched out of the database.
            assertEquals(scraperEntity, optionalScraperEntity.get());

            PostgreSQLScraperDAOTests.connection.commit();
        } catch (Exception e) {
            PostgreSQLScraperDAOTests.connection.rollback();

            throw e;
        }
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