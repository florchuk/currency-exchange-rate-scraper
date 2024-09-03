package org.example;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.Logger;
import org.example.dao.*;
import org.example.migrators.Migrator;
import org.example.migrators.PostgreSQLMigrator;
import org.example.scrapers.Scraper;
import org.example.seeders.PostgreSQLSeeder;
import org.example.seeders.Seeder;
import org.example.services.CurrencyExchangeRateService;
import org.example.services.ScraperService;
import org.example.utils.Config;
import org.example.utils.DBCPDataSource;
import org.example.utils.ScraperClassReader;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class Application {
    // Map of the supported migrators implementations.
    private final static Map<Class<? extends Driver>, Class<? extends Migrator>> supportedMigrators = new HashMap<>() {{
        put(org.postgresql.Driver.class, PostgreSQLMigrator.class);
    }};

    // Map of the supported seeders implementations.
    private final static Map<Class<? extends Driver>, Class<? extends Seeder>> supportedSeeders = new HashMap<>() {{
        put(org.postgresql.Driver.class, PostgreSQLSeeder.class);
    }};

    // Map of the supported DAOs implementations.
    private final static Map<Class<? extends Driver>, Class<? extends ScraperDAO>> supportedScraperDAOs = new HashMap<>() {{
        put(org.postgresql.Driver.class, PostgreSQLScraperDAO.class);
    }};

    private final static Map<Class<? extends Driver>, Class<? extends CurrencyExchangeRateDAO>> supportedCurrencyExchangeRateDAOs = new HashMap<>() {{
        put(org.postgresql.Driver.class, PostgreSQLCurrencyExchangeRateDAO.class);
    }};

    private final static Map<Class<? extends Driver>, Class<? extends CurrencyExchangeRateArchiveDAO>> supportedCurrencyExchangeRateArchiveDAOs = new HashMap<>() {{
        put(org.postgresql.Driver.class, PostgreSQLCurrencyExchangeRateArchiveDAO.class);
    }};

    private Application() {
        //
    }

    /**
     * Running the application.
     * @param logger Instance of the logger.
     * @throws Exception If fatal error occurs.
     */
    public static void run(Logger logger) throws Exception {
        // Reading the scraper classes.
        Queue<Class<? extends Scraper>> scraperClasses = ScraperClassReader
                .getClasses(Config.getProperty("scraperClassNames", "").split(","));

        // Getting JDBC driver class.
        Class<? extends Driver> driverClass = DriverManager.getDriver(Config.getProperty("database.url")).getClass();

        // Checking if there is implemented migrators, seeders and DAOs for specific driver.
        if (
                !Application.supportedMigrators.containsKey(driverClass)
                        || !Application.supportedSeeders.containsKey(driverClass)
                        || !Application.supportedScraperDAOs.containsKey(driverClass)
                        || !Application.supportedCurrencyExchangeRateDAOs.containsKey(driverClass)
                        || !Application.supportedCurrencyExchangeRateArchiveDAOs.containsKey(driverClass)
        ) {
            throw new ClassNotFoundException(
                    String.format("\"%s\" driver is not supported.", driverClass.getName())
            );
        }

        // Calculating the number of threads.
        int numberOfThreads =
                Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), scraperClasses.size()));

        // Creating of the Data Source.
        try (BasicDataSource dataSource = DBCPDataSource.getDataSource()) {
            // Setting initial and maximum number of connections (should be equal to number of the threads).
            dataSource.setInitialSize(numberOfThreads);
            dataSource.setMaxTotal(numberOfThreads);

            logger.info(
                    String.format(
                            "Data source is successfully created. The initial number of connections is %d, the maximum number connections is %d.",
                            dataSource.getInitialSize(),
                            dataSource.getMaxTotal()
                    )
            );

            // (Re)creating and (re)seeding of the database tables (if it is required).
            if (Boolean.parseBoolean(Config.getProperty("database.ddl", String.valueOf(false)))) {
                // Getting the migrator and the seeder.
                Migrator migrator = Application.supportedMigrators.get(driverClass).getConstructor().newInstance();
                Seeder seeder = Application.supportedSeeders.get(driverClass).getConstructor().newInstance();

                try (Connection connection = dataSource.getConnection()) {
                    try {
                        // Dropping of the database tables.
                        migrator.down(connection);
                        // Creation of the database tables.
                        migrator.up(connection);
                        // Seeding of the database tables.
                        seeder.up(connection);

                        connection.commit();
                    } catch (Exception e) {
                        connection.rollback();

                        throw e;
                    }
                }

                logger.info("The database are successfully (re)created and (re)seeded.");
            }

            // Creating the worker.
            Worker worker = new Worker(numberOfThreads);

            // Run the worker.
            worker.run(
                    // Creating the task for the worker.
                    new Task(
                            dataSource,
                            scraperClasses,
                            new ScraperService(
                                    Application.supportedScraperDAOs.get(driverClass).getConstructor().newInstance()
                            ),
                            new CurrencyExchangeRateService(
                                    Application.supportedCurrencyExchangeRateDAOs
                                            .get(driverClass).getConstructor().newInstance(),
                                    Application.supportedCurrencyExchangeRateArchiveDAOs
                                            .get(driverClass).getConstructor().newInstance()
                            ),
                            new ReentrantLock(),
                            logger
                    ),
                    logger
            );
        }
    }
}