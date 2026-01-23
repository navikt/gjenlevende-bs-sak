package no.nav.gjenlevende.bs.sak.client.domain

import java.util.UUID

data class Saksbehandler(
    val azureId: UUID,
    val navIdent: String,
    val fornavn: String,
    val etternavn: String,
    val enhet: String,
    val enhetsnavn: String = "",
)
