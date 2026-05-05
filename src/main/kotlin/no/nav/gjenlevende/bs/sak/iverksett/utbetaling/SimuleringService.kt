package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Tilgangskontroll
@Service
class SimuleringService(
    val utbetalingProducer: UtbetalingProducer,
) {
    fun simuler(behandling: Behandling) {
        val utbetalingMelding = lagUtbetalingMelding(behandling.id)
        utbetalingProducer.sendUtbetaling(behandling.id.toString(), utbetalingMelding)
    }

    fun lagUtbetalingMelding(behandlingId: UUID): UtbetalingMelding = UtbetalingMelding(UUID.randomUUID(), "sakId", behandlingId.toString(), "12345699999", "GJENLEVENDE_BARNETILSYN", LocalDateTime.now(), Periodetype.MND, listOf(), "Saksbehandler", "beslutter", true)
}
