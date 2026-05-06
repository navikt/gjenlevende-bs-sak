CREATE TABLE simulering
(
    id            UUID PRIMARY KEY,
    behandling_id UUID        NOT NULL REFERENCES behandling (id),
    status        TEXT        NOT NULL,
    respons       JSONB,
    opprettet_tid TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_simulering_behandling_id ON simulering (behandling_id);
