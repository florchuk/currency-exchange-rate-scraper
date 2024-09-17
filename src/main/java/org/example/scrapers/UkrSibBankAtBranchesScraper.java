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
import java.util.Objects;
import java.util.function.Predicate;

public class UkrSibBankAtBranchesScraper extends Scraper {
    /**
     * Constructor.
     * @param id The unique scraper identifier.
     */
    public UkrSibBankAtBranchesScraper(Integer id) {
        super(id);
    }

    @Override
    protected Object getRoot() throws Exception {
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
            return ((Document) root).selectXpath("//div[@id=\"kassa\"]/ul/li[@class=\"module-exchange__item\"]").toArray();
        }

        throw new IllegalArgumentException(
                String.format(
                        "Invalid root response Object (\"%s\").",
                        this.getClass().getName()
                )
        );
    }

    @Override
    protected Integer getUnit(Object item) throws Exception {
        return 1;
    }

    @Override
    protected String getUnitCurrencyCode(Object item) throws Exception {
        return Objects.requireNonNull(((Element) item).selectXpath("div[@class=\"module-exchange__item-currency\"]/div[@class=\"module-exchange__item-text\"]").first()).ownText().trim();
    }

    @Override
    protected String getRateCurrencyCode(Object item) throws Exception {
        return "UAH";
    }

    @Override
    protected Double getBuyRate(Object item) throws Exception {
        return Double.valueOf(((Element) item).selectXpath("div[@class=\"module-exchange__item-value\"][2]/div[@class=\"module-exchange__item-text\"]/span").text().trim());
    }

    @Override
    protected Double getSaleRate(Object item) throws Exception {
        return Double.valueOf(((Element) item).selectXpath("div[@class=\"module-exchange__item-value\"][4]/div[@class=\"module-exchange__item-text\"]/span").text().trim());
    }

    @Override
    protected Predicate<CurrencyExchangeRateDTO> getPredicate() {
        return new Predicate<>() {
            private final List<String> allowedCurrencyCodes = Arrays.stream(new String[]{"USD", "EUR", "UAH"}).toList();

            @Override
            public boolean test(CurrencyExchangeRateDTO currencyExchangeRateDTO) {
                return this.allowedCurrencyCodes.contains(currencyExchangeRateDTO.getUnitCurrencyCode())
                        && this.allowedCurrencyCodes.contains(currencyExchangeRateDTO.getRateCurrencyCode())
                        && !currencyExchangeRateDTO.getBuyRate().equals(0.0)
                        && !currencyExchangeRateDTO.getSaleRate().equals(0.0);
            }
        };
    }

    @Override
    protected URI getURI() throws URISyntaxException {
        return new URI("https://ukrsibbank.com/currency-cash/");
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
        return new String[]{
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        };
    }
}