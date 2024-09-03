package org.example.services;

import org.apache.commons.dbcp2.BasicDataSource;
import org.example.dao.PostgreSQLScraperDAO;
import org.example.entities.ScraperEntity;
import org.example.migrators.PostgreSQLMigrator;
import org.example.scrapers.Scraper;
import org.example.utils.Config;
import org.example.dto.CurrencyExchangeRateDTO;
import org.example.dto.ScraperDTO;
import org.example.dao.ScraperDAO;
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
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PostgreSQLScraperServiceTests {
    private static MockedStatic<Config> mockedStaticConfig;

    private static Migrator migrator;

    private static ScraperDAO scraperDAO;

    private static ScraperService scraperService;

    private static BasicDataSource dataSource;

    @BeforeAll
    public static void initAll() throws Exception {
        PostgreSQLScraperServiceTests.mockedStaticConfig = Mockito.mockStatic(Config.class);

        // Replacing database configuration properties.
        PostgreSQLScraperServiceTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.url"))
                .thenReturn("jdbc:h2:mem:postgres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        PostgreSQLScraperServiceTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.username"))
                .thenReturn("sa");
        PostgreSQLScraperServiceTests.mockedStaticConfig
                .when(() -> Config.getProperty("database.password"))
                .thenReturn("");

        PostgreSQLScraperServiceTests.migrator = new PostgreSQLMigrator();
        PostgreSQLScraperServiceTests.scraperDAO = new PostgreSQLScraperDAO();
        PostgreSQLScraperServiceTests.scraperService = new ScraperService(PostgreSQLScraperServiceTests.scraperDAO);

        PostgreSQLScraperServiceTests.dataSource = DBCPDataSource.getDataSource();

        try (Connection connection = PostgreSQLScraperServiceTests.dataSource.getConnection()) {
            try {
                // Creating tables of the database.
                PostgreSQLScraperServiceTests.migrator.up(connection);

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        }
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        try (Connection connection = PostgreSQLScraperServiceTests.dataSource.getConnection()) {
            try {
                // Dropping of the database tables.
                PostgreSQLScraperServiceTests.migrator.down(connection);

                connection.commit();
            } catch (Exception e) {
                connection.rollback();

                throw e;
            }
        } finally {
            PostgreSQLScraperServiceTests.dataSource.close();
            PostgreSQLScraperServiceTests.mockedStaticConfig.close();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (Connection connection = PostgreSQLScraperServiceTests.dataSource.getConnection()) {
            try (
                    PreparedStatement preparedStatement =
                            connection.prepareStatement("""
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
    public void readTest() throws Exception {
        // Creating instance on the DTO (for testing).
        ScraperDTO scraperDTO = new ScraperDTO(
                PostgreSQLScraperServiceTests.TestScraper.class,
                "Test",
                "Тест"
        );

        // Seeding the database.
        this.saveScraperDTO(scraperDTO);

        // Now database should contain the DTO.
        assertEquals(
                scraperDTO,
                PostgreSQLScraperServiceTests.scraperService.read(
                        PostgreSQLScraperServiceTests.dataSource,
                        new ScraperDTO(PostgreSQLScraperServiceTests.TestScraper.class)
                )
        );
    }

    @Test
    public void readThrowsNullPointerExceptionTest() {
        // At the beginning, database is empty.
        assertThrows(
                NullPointerException.class,
                () -> PostgreSQLScraperServiceTests.scraperService.read(
                        PostgreSQLScraperServiceTests.dataSource,
                        new ScraperDTO(PostgreSQLScraperServiceTests.TestScraper.class)
                )
        );
    }

    private void saveScraperDTO(ScraperDTO scraperDTO) throws Exception {
        try (Connection connection = PostgreSQLScraperServiceTests.dataSource.getConnection()) {
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