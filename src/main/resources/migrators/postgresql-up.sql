CREATE TABLE IF NOT EXISTS scrapers (
    id INTEGER,
    class_name CHARACTER VARYING(255) NOT NULL,
    name_en CHARACTER VARYING(255) NOT NULL,
    name_uk CHARACTER VARYING(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS scrapers_index ON scrapers (class_name);

CREATE SEQUENCE IF NOT EXISTS scrapers_sequence AS INTEGER INCREMENT BY 1 MINVALUE 1 START WITH 1;

CREATE TABLE IF NOT EXISTS currency_exchange_rates (
	id INTEGER,
	scraper_id INTEGER NOT NULL,
	unit INTEGER NOT NULL,
	unit_currency_code CHARACTER(3) NOT NULL,
	rate_currency_code CHARACTER(3) NOT NULL,
	buy_rate DOUBLE PRECISION NOT NULL,
	sale_rate DOUBLE PRECISION NOT NULL,
	created_at TIMESTAMP(6) NOT NULL,
	updated_at TIMESTAMP(6) NOT NULL,
	PRIMARY KEY (id),
    FOREIGN KEY (scraper_id) REFERENCES scrapers (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS currency_exchange_rates_index ON currency_exchange_rates (
    scraper_id,
    unit_currency_code,
    rate_currency_code
);

CREATE SEQUENCE IF NOT EXISTS currency_exchange_rates_sequence AS INTEGER INCREMENT BY 1 MINVALUE 1 START WITH 1;

CREATE TABLE IF NOT EXISTS currency_exchange_rates_archive (
	id BIGINT,
	currency_exchange_rate_id INTEGER NOT NULL,
	unit INTEGER NOT NULL,
	buy_rate DOUBLE PRECISION NOT NULL,
	sale_rate DOUBLE PRECISION NOT NULL,
	created_at TIMESTAMP(6) NOT NULL,
	updated_at TIMESTAMP(6) NOT NULL,
	PRIMARY KEY (id),
    FOREIGN KEY (currency_exchange_rate_id) REFERENCES currency_exchange_rates (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS currency_exchange_rates_archive_sequence AS BIGINT INCREMENT BY 1 MINVALUE 1 START WITH 1;