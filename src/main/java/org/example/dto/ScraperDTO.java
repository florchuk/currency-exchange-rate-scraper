package org.example.dto;

import org.example.entities.ScraperEntity;
import org.example.scrapers.Scraper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ScraperDTO {
    private Integer id;

    private Class<? extends Scraper> clazz;

    private String nameEn;

    private String nameUk;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ScraperDTO(
            Integer id,
            Class<? extends Scraper> clazz,
            String nameEn,
            String nameUk,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.clazz = clazz;
        this.nameEn = nameEn;
        this.nameUk = nameUk;
        this.createdAt = LocalDateTime.from(createdAt).truncatedTo(ChronoUnit.MICROS);
        this.updatedAt = LocalDateTime.from(updatedAt).truncatedTo(ChronoUnit.MICROS);
    }

    public ScraperDTO(
            Integer id,
            Class<? extends Scraper> clazz,
            String nameEn,
            String nameUk
    ) {
        this.id = id;
        this.clazz = clazz;
        this.nameEn = nameEn;
        this.nameUk = nameUk;

        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.from(createdAt);
    }

    public ScraperDTO(Class<? extends Scraper> clazz) {
        this.id = null;
        this.clazz = clazz;
        this.nameEn = null;
        this.nameUk = null;

        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.from(createdAt);
    }

    public ScraperDTO(Class<? extends Scraper> clazz, String nameEn, String nameUk) {
        this.id = null;
        this.clazz = clazz;
        this.nameEn = nameEn;
        this.nameUk = nameUk;

        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.from(createdAt);
    }

    public ScraperDTO(ScraperEntity scraperEntity) {
        this.id = scraperEntity.getId();
        this.clazz = scraperEntity.getClazz();
        this.nameEn = scraperEntity.getNameEn();
        this.nameUk = scraperEntity.getNameUk();

        if (scraperEntity.getCreatedAt() == null && scraperEntity.getUpdatedAt() == null) {
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

            this.createdAt = createdAt;
            this.updatedAt = LocalDateTime.from(createdAt);
        } else {
            this.createdAt = scraperEntity.getCreatedAt() != null
                    ? LocalDateTime.from(scraperEntity.getCreatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
            this.updatedAt = scraperEntity.getUpdatedAt() != null
                    ? LocalDateTime.from(scraperEntity.getUpdatedAt())
                    : LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Class<? extends Scraper> getClazz() {
        return this.clazz;
    }

    public void setClazz(Class<? extends Scraper> clazz) {
        this.clazz = clazz;
    }

    public String getNameEn() {
        return this.nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameUk() {
        return this.nameUk;
    }

    public void setNameUk(String nameUk) {
        this.nameUk = nameUk;
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

        ScraperDTO scraperDTO = (ScraperDTO) obj;

        return Objects.equals(this.id, scraperDTO.getId())
                && Objects.equals(
                        this.clazz != null ? this.clazz.getName() : null,
                        scraperDTO.getClazz() != null ? scraperDTO.getClazz().getName() : null
                )
                && Objects.equals(this.nameEn, scraperDTO.getNameEn())
                && Objects.equals(this.nameUk, scraperDTO.getNameUk())
                && Objects.equals(this.createdAt, scraperDTO.getCreatedAt())
                && Objects.equals(this.updatedAt, scraperDTO.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + (this.id == null ? 0 : this.id.hashCode());
        result = 31 * result + (this.clazz == null ? 0 : this.clazz.getName().hashCode());
        result = 31 * result + (this.nameEn == null ? 0 : this.nameEn.hashCode());
        result = 31 * result + (this.nameUk == null ? 0 : this.nameUk.hashCode());
        result = 31 * result + (this.createdAt == null ? 0 : this.createdAt.hashCode());
        result = 31 * result + (this.updatedAt == null ? 0 : this.updatedAt.hashCode());

        return result;
    }
}