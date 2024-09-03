package org.example.entities;

import org.example.dto.CurrencyExchangeRateDTO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CurrencyExchangeRateEntity {
    private Integer id;

    private Integer scraperId;

    private Integer unit;

    private String unitCurrencyCode;

    private String rateCurrencyCode;

    private Double buyRate;

    private Double saleRate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CurrencyExchangeRateEntity(
            Integer id,
            Integer scraperId,
            Integer unit,
            String unitCurrencyCode,
            String rateCurrencyCode,
            Double buyRate,
            Double saleRate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.scraperId = scraperId;
        this.unit = unit;
        this.unitCurrencyCode = unitCurrencyCode;
        this.rateCurrencyCode = rateCurrencyCode;
        this.buyRate = buyRate;
        this.saleRate = saleRate;
        this.createdAt = LocalDateTime.from(createdAt).truncatedTo(ChronoUnit.MICROS);
        this.updatedAt = LocalDateTime.from(updatedAt).truncatedTo(ChronoUnit.MICROS);
    }

    public CurrencyExchangeRateEntity(
            Integer id,
            Integer scraperId,
            Integer unit,
            String unitCurrencyCode,
            String rateCurrencyCode,
            Double buyRate,
            Double saleRate
    ) {
        this.id = id;
        this.scraperId = scraperId;
        this.unit = unit;
        this.unitCurrencyCode = unitCurrencyCode;
        this.rateCurrencyCode = rateCurrencyCode;
        this.buyRate = buyRate;
        this.saleRate = saleRate;

        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.from(createdAt);
    }

    public CurrencyExchangeRateEntity(CurrencyExchangeRateDTO currencyExchangeRateDTO) {
        this.id = null;
        this.scraperId = currencyExchangeRateDTO.getScraperId();
        this.unit = currencyExchangeRateDTO.getUnit();
        this.unitCurrencyCode = currencyExchangeRateDTO.getUnitCurrencyCode();
        this.rateCurrencyCode = currencyExchangeRateDTO.getRateCurrencyCode();
        this.buyRate = currencyExchangeRateDTO.getBuyRate();
        this.saleRate = currencyExchangeRateDTO.getSaleRate();

        if (currencyExchangeRateDTO.getCreatedAt() == null && currencyExchangeRateDTO.getUpdatedAt() == null) {
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

            this.createdAt = createdAt;
            this.updatedAt = LocalDateTime.from(createdAt);
        } else {
            this.createdAt = currencyExchangeRateDTO.getCreatedAt() != null
                    ? LocalDateTime.from(currencyExchangeRateDTO.getCreatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
            this.updatedAt = currencyExchangeRateDTO.getUpdatedAt() != null
                    ? LocalDateTime.from(currencyExchangeRateDTO.getUpdatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScraperId() {
        return this.scraperId;
    }

    public void setScraperId(Integer scraperId) {
        this.scraperId = scraperId;
    }

    public Integer getUnit() {
        return this.unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public String getUnitCurrencyCode() {
        return this.unitCurrencyCode;
    }

    public void setUnitCurrencyCode(String unitCurrencyCode) {
        this.unitCurrencyCode = unitCurrencyCode;
    }

    public String getRateCurrencyCode() {
        return this.rateCurrencyCode;
    }

    public void setRateCurrencyCode(String rateCurrencyCode) {
        this.rateCurrencyCode = rateCurrencyCode;
    }

    public Double getBuyRate() {
        return this.buyRate;
    }

    public void setBuyRate(Double buyRate) {
        this.buyRate = buyRate;
    }

    public Double getSaleRate() {
        return this.saleRate;
    }

    public void setSaleRate(Double saleRate) {
        this.saleRate = saleRate;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = LocalDateTime.from(createdAt).truncatedTo(ChronoUnit.MICROS);
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = LocalDateTime.from(updatedAt).truncatedTo(ChronoUnit.MICROS);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        CurrencyExchangeRateEntity currencyExchangeRateEntity = (CurrencyExchangeRateEntity) obj;

        return Objects.equals(this.id, currencyExchangeRateEntity.getId())
                && Objects.equals(this.scraperId, currencyExchangeRateEntity.getScraperId())
                && Objects.equals(this.unit, currencyExchangeRateEntity.getUnit())
                && Objects.equals(this.unitCurrencyCode, currencyExchangeRateEntity.getUnitCurrencyCode())
                && Objects.equals(this.rateCurrencyCode, currencyExchangeRateEntity.getRateCurrencyCode())
                && Objects.equals(this.buyRate, currencyExchangeRateEntity.getBuyRate())
                && Objects.equals(this.saleRate, currencyExchangeRateEntity.getSaleRate())
                && Objects.equals(this.createdAt, currencyExchangeRateEntity.getCreatedAt())
                && Objects.equals(this.updatedAt, currencyExchangeRateEntity.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + (this.id == null ? 0 : this.id.hashCode());
        result = 31 * result + (this.scraperId == null ? 0 : this.scraperId.hashCode());
        result = 31 * result + (this.unit == null ? 0 : this.unit.hashCode());
        result = 31 * result + (this.unitCurrencyCode == null ? 0 : this.unitCurrencyCode.hashCode());
        result = 31 * result + (this.rateCurrencyCode == null ? 0 : this.rateCurrencyCode.hashCode());
        result = 31 * result + (this.buyRate == null ? 0 : this.buyRate.hashCode());
        result = 31 * result + (this.saleRate == null ? 0 : this.saleRate.hashCode());
        result = 31 * result + (this.createdAt == null ? 0 : this.createdAt.hashCode());
        result = 31 * result + (this.updatedAt == null ? 0 : this.updatedAt.hashCode());

        return result;
    }
}