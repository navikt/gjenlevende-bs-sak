CREATE TABLE behandling_barn (
                                    id                  UUID         PRIMARY KEY,
                                    behandlingId        UUID         NOT NULL,
                                    soknad_barn_id      UUID,
                                    person_ident        VARCHAR,
                                    navn                VARCHAR,
                                    opprettetAv         VARCHAR       NOT NULL,
                                    opprettetTid        TIMESTAMP(3) NOT NULL DEFAULT localtimestamp,
                                    endretAv            VARCHAR       NOT NULL,
                                    endretTid           TIMESTAMP(3) NOT NULL DEFAULT localtimestamp,

                                    CONSTRAINT fk_behandling_barn_behandling_id FOREIGN KEY (behandlingId) REFERENCES behandling (id)
);
