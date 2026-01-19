CREATE TABLE vedtak (
                     behandlingId           UUID         NOT NULL,
                     resultatType           VARCHAR      NOT NULL,
                     begrunnelse            VARCHAR,
                     saksbehandlerIdent     VARCHAR      NOT NULL,
                     opphor_fom             TIMESTAMP(3) DEFAULT localtimestamp,
                     beslutterIdent         VARCHAR,
                     opprettet_tid          TIMESTAMP(3) NOT NULL DEFAULT localtimestamp,
                     opprettetAv            VARCHAR NOT NULL,

                     CONSTRAINT fk_vedtak_behandling_id FOREIGN KEY (behandlingId) REFERENCES behandling (id)
);

CREATE TABLE barnetilsynperiode (
                        behandlingId        UUID         NOT NULL,
                        dato_fra            TIMESTAMP(3) NOT NULL DEFAULT localtimestamp,
                        dato_til            TIMESTAMP(3) NOT NULL DEFAULT localtimestamp,
                        utgifter            INT NOT NULL,
                        barn                UUID[] NOT NULL,
                        periodetype         VARCHAR NOT NULL,
                        aktivitetstype      VARCHAR,

                        CONSTRAINT fk_barnetilsynperiode_behandling_id FOREIGN KEY (behandlingId) REFERENCES behandling (id)
);
