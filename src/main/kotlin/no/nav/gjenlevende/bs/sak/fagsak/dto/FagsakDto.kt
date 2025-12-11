package no.nav.gjenlevende.bs.sak.fagsak.dto

import no.nav.gjenlevende.bs.sak.fagsak.domain.Fagsak
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import java.util.UUID

data class FagsakDto(
    val id: UUID,
    val fagsakPersonId: UUID,
    val personident: String,
    val stønadstype: StønadType,
    val eksternId: Long,
)

fun Fagsak.tilDto(): FagsakDto =
    FagsakDto(
        id = this.id,
        fagsakPersonId = this.fagsakPersonId,
        personident = this.hentAktivIdent(),
        stønadstype = this.stønadstype,
        eksternId = this.eksternId,
    )
