package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType

data class FagsakRequest(
    val personIdent: String,
    val stønadstype: StønadType,
)
