package org.example.dto;

import org.example.entities.CurrencyExchangeRateArchiveEntity;
import org.example.entities.CurrencyExchangeRateEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CurrencyExchangeRateArchiveDTO {
    private Integer currencyExchangeRateId;

    private Integer unit;

    private Double buyRate;

    private Double saleRate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CurrencyExchangeRateArchiveDTO(
            Integer currencyExchangeRateId,
            Integer unit,
            Double buyRate,
            Double saleRate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.currencyExchangeRateId = currencyExchangeRateId;
        this.unit = unit;
        this.buyRate = buyRate;
        this.saleRate = saleRate;
        this.createdAt = LocalDateTime.from(createdAt).truncatedTo(ChronoUnit.MICROS);
        this.updatedAt = LocalDateTime.from(updatedAt).truncatedTo(ChronoUnit.MICROS);
    }

    public CurrencyExchangeRateArchiveDTO(
            Integer currencyExchangeRateId,
            Integer unit,
            Double buyRate,
            Double saleRate
    ) {
        this.currencyExchangeRateId = currencyExchangeRateId;
        this.unit = unit;
        this.buyRate = buyRate;
        this.saleRate = saleRate;

        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.from(createdAt);
    }

    public CurrencyExchangeRateArchiveDTO(CurrencyExchangeRateEntity currencyExchangeRateEntity) {
        this.currencyExchangeRateId = currencyExchangeRateEntity.getId();
        this.unit = currencyExchangeRateEntity.getUnit();
        this.buyRate = currencyExchangeRateEntity.getBuyRate();
        this.saleRate = currencyExchangeRateEntity.getSaleRate();

        if (currencyExchangeRateEntity.getCreatedAt() == null && currencyExchangeRateEntity.getUpdatedAt() == null) {
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

            this.createdAt = createdAt;
            this.updatedAt = LocalDateTime.from(createdAt);
        } else {
            this.createdAt = currencyExchangeRateEntity.getCreatedAt() != null
                    ? LocalDateTime.from(currencyExchangeRateEntity.getCreatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
            this.updatedAt = currencyExchangeRateEntity.getUpdatedAt() != null
                    ? LocalDateTime.from(currencyExchangeRateEntity.getUpdatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public CurrencyExchangeRateArchiveDTO(CurrencyExchangeRateArchiveEntity currencyExchangeRateArchiveEntity) {
        this.currencyExchangeRateId = currencyExchangeRateArchiveEntity.getCurrencyExchangeRateId();
        this.unit = currencyExchangeRateArchiveEntity.getUnit();
        this.buyRate = currencyExchangeRateArchiveEntity.getBuyRate();
        this.saleRate = currencyExchangeRateArchiveEntity.getSaleRate();

        if (currencyExchangeRateArchiveEntity.getCreatedAt() == null && currencyExchangeRateArchiveEntity.getUpdatedAt() == null) {
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

            this.createdAt = createdAt;
            this.updatedAt = LocalDateTime.from(createdAt);
        } else {
            this.createdAt = currencyExchangeRateArchiveEntity.getCreatedAt() != null
                    ? LocalDateTime.from(currencyExchangeRateArchiveEntity.getCreatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
            this.updatedAt = currencyExchangeRateArchiveEntity.getUpdatedAt() != null
                    ? LocalDateTime.from(currencyExchangeRateArchiveEntity.getUpdatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public Integer getCurrencyExchangeRateId() {
        return this.currencyExchangeRateId;
    }

    public void setCurrencyExchangeRateId(Integer currencyExchangeRateId) {
        this.currencyExchangeRateId = currencyExchangeRateId;
    }

    public Integer getUnit() {
        return this.unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
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

        CurrencyExchangeRateArchiveDTO currencyExchangeRateArchiveDTO = (CurrencyExchangeRateArchiveDTO) obj;

        return Objects.equals(this.currencyExchangeRateId, currencyExchangeRateArchiveDTO.getCurrencyExchangeRateId())
                && Objects.equals(this.unit, currencyExchangeRateArchiveDTO.getUnit())
                && Objects.equals(this.buyRate, currencyExchangeRateArchiveDTO.getBuyRate())
                && Objects.equals(this.saleRate, currencyExchangeRateArchiveDTO.getSaleRate())
                && Objects.equals(this.createdAt, currencyExchangeRateArchiveDTO.getCreatedAt())
                && Objects.equals(this.updatedAt, currencyExchangeRateArchiveDTO.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + (this.currencyExchangeRateId == null ? 0 : this.currencyExchangeRateId.hashCode());
        result = 31 * result + (this.unit == null ? 0 : this.unit.hashCode());
        result = 31 * result + (this.buyRate == null ? 0 : this.buyRate.hashCode());
        result = 31 * result + (this.saleRate == null ? 0 : this.saleRate.hashCode());
        result = 31 * result + (this.createdAt == null ? 0 : this.createdAt.hashCode());
        result = 31 * result + (this.updatedAt == null ? 0 : this.updatedAt.hashCode());

        return result;
    }
}