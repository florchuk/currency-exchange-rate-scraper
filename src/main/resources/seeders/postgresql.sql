INSERT INTO scrapers (
    id, class_name, name_en, name_uk, created_at, updated_at
) VALUES (
            NEXTVAL('scrapers_sequence'),
            'org.example.scrapers.NBUScraper',
            'National Bank of Ukraine',
            'Національний банк України',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        ),
        (
            NEXTVAL('scrapers_sequence'),
            'org.example.scrapers.PrivatBankAtBranchesScraper',
            'PrivatBank (at branches)',
            'ПриватБанк (у відділеннях)',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        ),
        (
            NEXTVAL('scrapers_sequence'),
            'org.example.scrapers.UkrSibBankAtBranchesScraper',
            'UKRSIBBANK (at branches)',
            'УКРСИББАНК (у відділеннях)',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
ON CONFLICT DO NOTHING;