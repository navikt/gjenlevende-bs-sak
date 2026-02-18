package no.nav.gjenlevende.bs.sak.behandling

import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringType
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BehandlingService(
    private val behandlingRepository: BehandlingRepository,
    private val lagBehandleSakOppgaveTask: LagBehandleSakOppgaveTask,
    private val endringshistorikkService: EndringshistorikkService,
) {
    @Transactional
    fun opprettBehandling(
        fagsakId: UUID,
        status: BehandlingStatus = BehandlingStatus.OPPRETTET,
    ): Behandling {
        val forrigeBehandlingId = behandlingRepository.finnSisteIverksatteBehandling(fagsakId)?.id

        val behandling =
            Behandling(
                fagsakId = fagsakId,
                status = status,
                resultat = BehandlingResultat.IKKE_SATT,
                forrigeBehandlingId = forrigeBehandlingId,
            )

        behandlingRepository.insert(
            behandling,
        )

        lagBehandleSakOppgaveTask.opprettBehandleSakOppgaveTask(behandling = behandling, saksbehandler = SikkerhetContext.hentSaksbehandler())

        endringshistorikkService.registrerEndring(
            behandlingId = behandling.id,
            endringType = EndringType.BEHANDLING_OPPRETTET,
        )

        return behandling
    }

    fun hentBehandling(behandlingId: UUID): Behandling? = behandlingRepository.findByIdOrNull(behandlingId)

    fun hentBehandlingerFraFagsak(fagsakId: UUID): List<Behandling>? = behandlingRepository.findAllByFagsakId(fagsakId)

    fun finnesÅpenBehandling(fagsakId: UUID) = behandlingRepository.existsByFagsakIdAndStatusIsNot(fagsakId, BehandlingStatus.FERDIGSTILT)

    fun oppdaterBehandlingStatus(
        behandlingId: UUID,
        status: BehandlingStatus,
    ) {
        val behandling = behandlingRepository.findByIdOrNull(behandlingId) ?: error("Fant ikke behandling med id=$behandlingId for oppdatering av BehandlingStatus")
        val oppdatertBehandling =
            behandling.copy(
                status = status,
            )
        behandlingRepository.update(oppdatertBehandling)
    }
}
