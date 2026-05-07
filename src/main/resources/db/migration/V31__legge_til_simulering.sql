CREATE TABLE simulering
(
    behandling_id UUID        PRIMARY KEY REFERENCES behandling (id),
    status        TEXT        NOT NULL,
    respons       JSONB,
    opprettet_tid TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_simulering_behandling_id ON simulering (behandling_id);
