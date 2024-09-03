package org.example;

import org.apache.logging.log4j.Logger;
import org.example.dto.CurrencyExchangeRateDTO;
import org.example.dto.ScraperDTO;
import org.example.scrapers.Scraper;
import org.example.services.CurrencyExchangeRateService;
import org.example.services.ScraperService;

import javax.sql.DataSource;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Task implements Runnable {
    private final DataSource dataSource;

    private final Queue<Class<? extends Scraper>> scraperClasses;

    private final ScraperService scraperService;

    private final CurrencyExchangeRateService currencyExchangeRateService;

    private final Lock lock;

    private final Logger logger;

    /**
     * Constructor.
     * @param dataSource Instance of the data source.
     * @param scraperClasses Queue of the scraper classes.
     * @param scraperService Instance of the service.
     * @param currencyExchangeRateService Instance of the service.
     * @param lock Instance of the thread lock.
     * @param logger Instance of the logger.
     */
    public Task(
            DataSource dataSource,
            Queue<Class<? extends Scraper>> scraperClasses,
            ScraperService scraperService,
            CurrencyExchangeRateService currencyExchangeRateService,
            Lock lock,
            Logger logger
    ) {
        this.dataSource = dataSource;
        this.scraperClasses = scraperClasses;
        this.scraperService = scraperService;
        this.currencyExchangeRateService = currencyExchangeRateService;
        this.lock = lock;
        this.logger = logger;
    }

    @Override
    public void run() {
        int successCounter = 0;
        int failureCounter = 0;

        this.logger.info("Thread successfully started.");

        // Running until all the scraper classes is over (will not be processed).
        while (true) {
            Class<? extends Scraper> scraperClass;

            try {
                // Locking queue, before fetching scraper class from the queue.
                this.lock.lock();

                // Fetching scraper class from the queue.
                scraperClass = this.scraperClasses.remove();
            } catch (NoSuchElementException e) {
                this.logger.info("There is no items in the queue left.");

                // Exiting out of the task.
                break;
            } finally {
                // Unlocking queue, after fetching scraper class from the queue.
                this.lock.unlock();
            }

            try {
                Scraper scraper = scraperClass.getConstructor(Integer.class).newInstance(
                        this.scraperService.read(this.dataSource, new ScraperDTO(scraperClass)).getId()
                );

                this.logger.info(
                        String.format(
                                "The instance of \"%s\" is successfully created.",
                                scraperClass.getName()
                        )
                );

                CurrencyExchangeRateDTO[] currencyExchangeRateDTOs = scraper.getCurrencyExchangeRateDTOs();

                this.logger.info(
                        String.format(
                                "%d entities is successfully fetched by \"%s\".",
                                currencyExchangeRateDTOs.length,
                                scraperClass.getName()
                        )
                );

                this.currencyExchangeRateService.save(this.dataSource, currencyExchangeRateDTOs);

                this.logger.info(
                        String.format(
                                "%d entities (fetched by \"%s\") is successfully proceeded by \"%s\".",
                                currencyExchangeRateDTOs.length,
                                scraperClass.getName(),
                                this.currencyExchangeRateService.getClass().getName()
                        )
                );

                ++successCounter;
            } catch (Exception e) {
                this.logger.error(e.getMessage(), e);

                ++failureCounter;
            }
        }

        this.logger.info(
                String.format(
                        "Thread successfully ended. Successfully proceeded %d operations, failed %d.",
                        successCounter,
                        failureCounter
                )
        );
    }
}