CREATE TABLE brev_mottaker
(
    id                    UUID PRIMARY KEY,
    behandling_id         UUID NOT NULL REFERENCES behandling (id) ON DELETE CASCADE,

    person_rolle          TEXT NOT NULL,
    mottaker_type         TEXT NOT NULL,

    person_ident          TEXT NULL,
    orgnr                 TEXT NULL,

    navn_hos_organisasjon TEXT NULL,

    CONSTRAINT chk_mottaker_type_fields CHECK (
        (mottaker_type = 'PERSON' AND person_ident IS NOT NULL AND orgnr IS NULL)
            OR
        (mottaker_type = 'ORGANISASJON' AND orgnr IS NOT NULL AND person_ident IS NULL)
        )
);

CREATE INDEX idx_brev_mottaker_behandling_id ON brev_mottaker (behandling_id);
CREATE INDEX idx_brev_mottaker_rolle ON brev_mottaker (person_rolle);
CREATE INDEX idx_brev_mottaker_orgnr ON brev_mottaker (orgnr) WHERE orgnr IS NOT NULL;
CREATE INDEX idx_brev_mottaker_person_ident ON brev_mottaker (person_ident) WHERE person_ident IS NOT NULL;
