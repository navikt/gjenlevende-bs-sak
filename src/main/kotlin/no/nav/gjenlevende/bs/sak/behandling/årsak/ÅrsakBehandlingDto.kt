package no.nav.gjenlevende.bs.sak.behandling.årsak

import java.time.LocalDate
import java.util.UUID

data class ÅrsakBehandlingDto(
    val behandlingId: UUID,
    val kravdato: LocalDate,
    val årsak: Årsak,
    val beskrivelse: String,
)

fun ÅrsakBehandling.tilDto() =
    ÅrsakBehandlingDto(
        behandlingId = this.behandlingId,
        kravdato = this.kravdato,
        årsak = this.årsak,
        beskrivelse = this.beskrivelse,
    )
