package org.example.scrapers;

import org.example.dto.CurrencyExchangeRateDTO;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

public abstract class Scraper {
    private final Integer id;

    /**
     * Constructor.
     * @param id The unique scraper identifier.
     */
    public Scraper(Integer id) {
        this.id = id;
    }

    /**
     * Getting array of the DTOs.
     * @return Array of the DTOs.
     * @throws Exception If a some error occurs.
     */
    public CurrencyExchangeRateDTO[] getCurrencyExchangeRateDTOs() throws Exception {
        Object root = this.getRoot();
        Object[] items = this.getItems(root);

        if (items.length == 0) {
            throw new Exception(
                    String.format(
                            "Items to read was not found (\"%s\").",
                            this.getClass().getName()
                    )
            );
        }

        CurrencyExchangeRateDTO[] currencyExchangeRateDTOs = new CurrencyExchangeRateDTO[items.length];

        for (int i = 0; i < items.length; i++) {
            Object item = items[i];

            currencyExchangeRateDTOs[i] = new CurrencyExchangeRateDTO(
                    this.getId(),
                    this.getUnit(item),
                    this.getUnitCurrencyCode(item),
                    this.getRateCurrencyCode(item),
                    this.getBuyRate(item),
                    this.getSaleRate(item)
            );
        }

        return Arrays.stream(currencyExchangeRateDTOs)
                .filter(this.getPredicate()).toList().toArray(new CurrencyExchangeRateDTO[0]);
    }

    /**
     * Getting the root Object, based on the response, for further processing.
     * @return Root Object, based on the response, for further processing.
     * @throws Exception If a creation of root response Object error occurs.
     */
    protected Object getRoot() throws Exception {
        return this.getResponse();
    }

    /**
     * Getting request response as the string.
     * @return Request response as the string.
     * @throws Exception If a request error occurs.
     */
    protected String getResponse() throws Exception {
        try (HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();

            httpRequestBuilder
                    .uri(this.getURI())
                    .timeout(this.getTimeout())
                    .method(this.getMethod(), this.getBodyPublisher());

            String[] headers = this.getHeaders();

            if (headers.length > 0) {
                httpRequestBuilder.headers(headers);
            }

            HttpRequest httpRequest = httpRequestBuilder.build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception(
                        String.format(
                                "Response status code is %d (\"%s\").",
                                httpResponse.statusCode(),
                                httpResponse.uri().toString()
                        )
                );
            }

            return httpResponse.body();
        }
    }

    /**
     * Getting the unique scraper identifier.
     * @return The unique scraper identifier
     */
    protected Integer getId() {
        return this.id;
    }

    /**
     * Getting array of Object items, where each Object item contains the values, for creation exactly one item of the DTO.
     * @param root Root object, based on the response.
     * @return Object items, where each Object item contains the values, for creation exactly one item of the DTO.
     * @throws Exception If a creation of Object items error occurs.
     */
    protected abstract Object[] getItems(Object root) throws Exception;

    /**
     * Getting the unit value.
     * @param item Object item, that contains the unit value.
     * @return The unit value.
     * @throws Exception If a reading value error occurs.
     */
    protected abstract Integer getUnit(Object item) throws Exception;

    /**
     * Getting the unit currency code value.
     * @param item Object item, that contains the unit currency code value.
     * @return The unit currency code value.
     * @throws Exception If a reading value error occurs.
     */
    protected abstract String getUnitCurrencyCode(Object item) throws Exception;

    /**
     * Getting the rate currency code value.
     * @param item Object item, that contains the rate currency code value.
     * @return The rate currency code value.
     * @throws Exception If a reading value error occurs.
     */
    protected abstract String getRateCurrencyCode(Object item) throws Exception;

    /**
     * Getting the buy rate value.
     * @param item Object item, that contains the buy rate value.
     * @return The buy rate value.
     * @throws Exception If a reading value error occurs.
     */
    protected abstract Double getBuyRate(Object item) throws Exception;

    /**
     * Getting the sale rate value.
     * @param item Object item, that contains the sale rate value.
     * @return The sale rate value.
     * @throws Exception If a reading value error occurs.
     */
    protected abstract Double getSaleRate(Object item) throws Exception;

    /**
     * Getting predicate instance, that will be used for filtering the final results.
     * @return Predicate instance.
     */
    protected abstract Predicate<CurrencyExchangeRateDTO> getPredicate();

    /**
     * Request uri.
     * @return Request URI instance.
     * @throws URISyntaxException If a URI error occurs.
     */
    protected abstract URI getURI() throws URISyntaxException;

    /**
     * Getting request timeout.
     * @return Request timeout, as Duration instance.
     */
    protected abstract Duration getTimeout();

    /**
     * Getting request method.
     * @return Request method.
     */
    protected abstract String getMethod();

    /**
     * Getting request body publisher.
     * @return Request body publisher.
     */
    protected abstract HttpRequest.BodyPublisher getBodyPublisher();

    /**
     * Getting headers that will be added to request.
     * @return Array of pairs - header name and header value, or empty array, if there is no headers required.
     */
    protected abstract String[] getHeaders();
}