package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Tilgangskontroll
@Service
class SimuleringService(
    val utbetalingProducer: UtbetalingProducer,
    val simuleringRepository: SimuleringRepository,
) {
    @Transactional
    fun simuler(behandlingId: UUID) {
        val melding = lagUtbetalingMelding(behandlingId)

        if (simuleringRepository.existsById(behandlingId)) {
            simuleringRepository.update(
                Simulering(
                    behandlingId = behandlingId,
                    status = SimuleringStatus.VENTER,
                ),
            )
        } else {
            simuleringRepository.insert(
                Simulering(
                    behandlingId = behandlingId,
                    status = SimuleringStatus.VENTER,
                ),
            )
        }

        utbetalingProducer.sendSimulering(behandlingId, melding)
    }

    fun hentSimulering(behandlingId: UUID): Simulering? = simuleringRepository.findById(behandlingId).orElse(null)

    fun lagUtbetalingMelding(
        behandlingId: UUID,
    ): UtbetalingMelding =
        UtbetalingMelding(
            behandlingId = behandlingId,
            sakId = "sakId",
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
