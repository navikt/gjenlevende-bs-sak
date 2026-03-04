package no.nav.gjenlevende.bs.sak.barn

import no.nav.gjenlevende.bs.sak.fagsak.domain.StønadType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class HentBarnRequest(
    val personIdent: String,
    val fagsakPersonId: UUID,
    val stønadstype: StønadType,
)

data class LagreBarnRequest(
    val behandlingId: UUID,
    val barn: List<Barn>,
)

data class Barn(
    val personIdent: String,
    val navn: String,
    val fødselsdato: LocalDate,
    val hentetTidspunkt: LocalDateTime,
)

data class HentBarnResponse(
    val personIdent: String,
    val navn: String,
    val fødselsdato: LocalDate,
    val hentetTidspunkt: LocalDateTime,
)
