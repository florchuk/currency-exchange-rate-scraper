package org.example.scrapers;

import org.example.dto.CurrencyExchangeRateDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UkrSibBankAtBranchesScraperEntityTests {
    private static String response;

    @Spy
    private final Scraper scraper = new UkrSibBankAtBranchesScraper(1);

    // Expected data. Sorted by unit currency code.
    private final List<CurrencyExchangeRateDTO> expectedCurrencyExchangeRateDTOs = Arrays.stream(
            new CurrencyExchangeRateDTO[]{
                    new CurrencyExchangeRateDTO(
                            this.scraper.getId(),
                            1,
                            "USD",
                            "UAH",
                            1.01,
                            2.02
                    ),
                    new CurrencyExchangeRateDTO(
                            this.scraper.getId(),
                            1,
                            "EUR",
                            "UAH",
                            3.03,
                            4.04
                    )
            }).sorted(Comparator.comparing(CurrencyExchangeRateDTO::getUnitCurrencyCode)).toList();

    @BeforeAll
    public static void initAll() throws IOException, NullPointerException {
        try (
                InputStream inputStream =
                        UkrSibBankAtBranchesScraperEntityTests.class.getClassLoader()
                                .getResourceAsStream("scrapers/response-ukrsibbank-at-branches.html")
        ) {
            UkrSibBankAtBranchesScraperEntityTests.response = new String(Objects.requireNonNull(inputStream).readAllBytes());
        }
    }

    @Test
    public void getExchangeRateDTOsTest() throws Exception {
        // Replacing response for the testing.
        Mockito.doReturn(UkrSibBankAtBranchesScraperEntityTests.response).when(this.scraper).getResponse();

        // Getting data from testing response.
        CurrencyExchangeRateDTO[] currencyExchangeRateDTOs = this.scraper.getCurrencyExchangeRateDTOs();

        // Size of expected and tested response data should be equal.
        assertEquals(this.expectedCurrencyExchangeRateDTOs.size(), currencyExchangeRateDTOs.length);

        // Sorting by unit currency code.
        List<CurrencyExchangeRateDTO> actualCurrencyExchangeRateDTOs =
                Arrays.stream(currencyExchangeRateDTOs)
                        .sorted(Comparator.comparing(CurrencyExchangeRateDTO::getUnitCurrencyCode))
                        .toList();

        // Synchronizing when was created and when was updated the DTOs (for expected and tested response data).
        this.expectedCurrencyExchangeRateDTOs.get(0).setCreatedAt(actualCurrencyExchangeRateDTOs.get(0).getCreatedAt());
        this.expectedCurrencyExchangeRateDTOs.get(0).setUpdatedAt(actualCurrencyExchangeRateDTOs.get(0).getUpdatedAt());

        this.expectedCurrencyExchangeRateDTOs.get(1).setCreatedAt(actualCurrencyExchangeRateDTOs.get(1).getCreatedAt());
        this.expectedCurrencyExchangeRateDTOs.get(1).setUpdatedAt(actualCurrencyExchangeRateDTOs.get(1).getUpdatedAt());

        // Expected and tested response data should be equal.
        assertEquals(this.expectedCurrencyExchangeRateDTOs, actualCurrencyExchangeRateDTOs);
    }
}