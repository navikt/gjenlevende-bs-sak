CREATE TABLE iverksett
(
    behandling_id          UUID PRIMARY KEY,
    eksternReferanse_id    UUID,
    journalpostResultat_id TEXT,
    opprettet_av           VARCHAR      NOT NULL DEFAULT 'VL',
    opprettet_tid          TIMESTAMP(3) NOT NULL DEFAULT LOCALTIMESTAMP,
    endret_av              VARCHAR      NOT NULL,
    endret_tid             TIMESTAMP(3) NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT fk_iverksett_behandling FOREIGN KEY (behandling_id) REFERENCES behandling (id)
);
