package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Tilgangskontroll
@Service
class SimuleringService(
    val utbetalingProducer: UtbetalingProducer,
    val simuleringRepository: SimuleringRepository,
    @Autowired(required = false) val devAutoCompleter: DevSimuleringAutoCompleter? = null,
) {
    fun simuler(behandling: Behandling): UUID {
        val simuleringId = UUID.randomUUID()
        val melding = lagUtbetalingMelding(simuleringId, behandling.id)

        simuleringRepository.insert(
            Simulering(
                id = simuleringId,
                behandlingId = behandling.id,
                status = SimuleringStatus.VENTER,
            ),
        )

        utbetalingProducer.sendSimulering(simuleringId.toString(), melding)
        devAutoCompleter?.autoFullfor(simuleringId)
        return simuleringId
    }

    fun hentSimulering(simuleringId: UUID): Simulering? = simuleringRepository.findById(simuleringId).orElse(null)

    fun lagUtbetalingMelding(
        simuleringId: UUID,
        behandlingId: UUID,
    ): UtbetalingMelding =
        UtbetalingMelding(
            id = simuleringId,
            sakId = "sakId",
            behandlingId = behandlingId.toString(),
            personident = "12345699999",
            stønad = "GJENLEVENDE_BARNETILSYN",
            vedtakstidspunkt = LocalDateTime.now(),
            periodetype = Periodetype.MND,
            perioder = listOf(),
            saksbehandler = "Saksbehandler",
            beslutter = "beslutter",
            dryrun = true,
        )
}
