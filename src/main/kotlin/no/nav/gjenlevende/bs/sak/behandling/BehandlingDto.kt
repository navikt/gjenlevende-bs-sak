package no.nav.gjenlevende.bs.sak.behandling

import java.time.LocalDateTime
import java.util.UUID

data class BehandlingDto(
    val id: UUID,
    val fagsakId: UUID,
    val status: BehandlingStatus,
    val sistEndret: LocalDateTime,
    val opprettet: LocalDateTime,
    val opprettetAv: String,
    val resultat: BehandlingResultat,
)

fun Behandling.tilDto(): BehandlingDto =
    BehandlingDto(
        id = this.id,
        fagsakId = this.fagsakId,
        status = this.status,
        sistEndret = this.sporing.endretTid,
        opprettet = this.sporing.opprettetTid,
        opprettetAv = this.sporing.opprettetAv,
        resultat = this.resultat,
    )
