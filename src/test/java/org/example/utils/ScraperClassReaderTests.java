package org.example.utils;

import org.example.dto.CurrencyExchangeRateDTO;
import org.example.scrapers.Scraper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScraperClassReaderTests {
    @Test
    public void getClassesTest() throws Exception {
        // Class names for testing.
        String[] expectedClassNames = new String[]{
                ScraperClassReaderTests.FirstScraper.class.getName(),
                ScraperClassReaderTests.SecondScraper.class.getName()
        };

        // Getting classes.
        Queue<Class<? extends Scraper>> classes = ScraperClassReader.getClasses(expectedClassNames);

        // Comparing.
        assertEquals(
                Arrays.stream(expectedClassNames).sorted(String::compareTo).toList(),
                classes.stream().map(Class::getName).sorted(String::compareTo).toList()
        );
    }

    @Test
    public void getClassesThrowsIllegalArgumentExceptionTest() throws Exception {
        // The class names is empty.
        assertThrows(
                IllegalArgumentException.class,
                () -> ScraperClassReader.getClasses("")
        );

        // One of the class names is empty.
        assertThrows(
                IllegalArgumentException.class,
                () -> ScraperClassReader.getClasses(
                        new String[]{
                                ScraperClassReaderTests.FirstScraper.class.getName(),
                                "",
                                ScraperClassReaderTests.SecondScraper.class.getName()
                        }
                )
        );

        // The core class is not allowed.
        assertThrows(
                IllegalArgumentException.class,
                () -> ScraperClassReader.getClasses(Scraper.class.getName())
        );

        // The abstract classes is not allowed.
        assertThrows(
                IllegalArgumentException.class,
                () -> ScraperClassReader.getClasses(
                        new String[]{
                                ScraperClassReaderTests.FirstScraper.class.getName(),
                                ScraperClassReaderTests.FirstAbstractScraper.class.getName(),
                                ScraperClassReaderTests.SecondAbstractScraper.class.getName()
                        }
                )
        );

        // Classes that not extend the core class is not allowed.
        assertThrows(
                IllegalArgumentException.class,
                () -> ScraperClassReader.getClasses(Object.class.getName())
        );
    }

    @Test
    public void getClassesThrowsClassNotFoundExceptionTest() throws Exception {
        // Not existing classes is not allowed.
        assertThrows(
                Exception.class,
                () -> ScraperClassReader.getClasses("org.example.scrapers.NotExistingScraper")
        );
    }

    // First abstract class for testing.
    private abstract static class FirstAbstractScraper extends Scraper {
        public FirstAbstractScraper(Integer id) {
            super(id);
        }
    }

    // Second abstract class for testing.
    private abstract static class SecondAbstractScraper extends Scraper {
        public SecondAbstractScraper(Integer id) {
            super(id);
        }
    }

    // First class for testing.
    private static class FirstScraper extends Scraper {
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

    // Second class for testing.
    private static class SecondScraper extends Scraper {
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