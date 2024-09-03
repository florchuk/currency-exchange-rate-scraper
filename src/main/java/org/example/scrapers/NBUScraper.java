package org.example.scrapers;

import org.example.dto.CurrencyExchangeRateDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class NBUScraper extends Scraper {
    /**
     * Constructor.
     * @param id The unique scraper identifier.
     */
    public NBUScraper(Integer id) {
        super(id);
    }

    @Override
    public Object getRoot() throws Exception {
        Object root = super.getRoot();

        if (root instanceof String) {
            return Jsoup.parse((String) root);
        }

        throw new IllegalArgumentException(
                String.format(
                        "Invalid root response Object (\"%s\").",
                        this.getClass().getName()
                )
        );
    }

    @Override
    protected Object[] getItems(Object root) throws Exception {
        if (root instanceof Document) {
            return ((Document) root).selectXpath("//table[@id=\"exchangeRates\"]/tbody/tr").toArray();
        }

        throw new Exception(
                String.format(
                        "Invalid root response Object (\"%s\").",
                        this.getClass().getName()
                )
        );
    }

    @Override
    protected Integer getUnit(Object item) throws Exception {
        return Integer.valueOf(((Element) item).selectXpath("td[3]").text().trim());
    }

    @Override
    protected String getUnitCurrencyCode(Object item) throws Exception {
        return ((Element) item).selectXpath("td[2]").text().trim();
    }

    @Override
    protected String getRateCurrencyCode(Object item) throws Exception {
        return "UAH";
    }

    @Override
    protected Double getBuyRate(Object item) throws Exception {
        return Double.valueOf(((Element) item).selectXpath("td[5]").text().trim().replace(',', '.'));
    }

    @Override
    protected Double getSaleRate(Object item) throws Exception {
        return this.getBuyRate(item);
    }

    @Override
    protected Predicate<CurrencyExchangeRateDTO> getPredicate() {
        return new Predicate<>() {
            private final List<String> allowedCurrencyCodes = Arrays.stream(new String[]{"USD", "EUR", "UAH"}).toList();

            @Override
            public boolean test(CurrencyExchangeRateDTO currencyExchangeRateDTO) {
                return this.allowedCurrencyCodes.contains(currencyExchangeRateDTO.getUnitCurrencyCode())
                        && this.allowedCurrencyCodes.contains(currencyExchangeRateDTO.getRateCurrencyCode());
            }
        };
    }

    @Override
    protected URI getURI() throws URISyntaxException {
        return new URI("https://bank.gov.ua/ua/markets/exchangerates");
    }

    @Override
    protected Duration getTimeout() {
        return Duration.ofSeconds(30);
    }

    @Override
    protected String getMethod() {
        return "GET";
    }

    @Override
    protected HttpRequest.BodyPublisher getBodyPublisher() {
        return HttpRequest.BodyPublishers.noBody();
    }

    @Override
    protected String[] getHeaders() {
        return new String[0];
    }
}