package no.nav.gjenlevende.bs.sak.behandling

import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class BehandlingService(
    private val behandlingRepository: BehandlingRepository,
    private val taskService: TaskService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun opprettBehandling(
        fagsakId: UUID,
        status: BehandlingStatus = BehandlingStatus.OPPRETTET,
    ): Behandling {
        val behandling =
            Behandling(
                fagsakId = fagsakId,
                status = status,
                resultat = BehandlingResultat.IKKE_SATT,
            )

        behandlingRepository.insert(
            behandling,
        )

        opprettBehandleSakOppgaveTask(behandling)

        return behandling
    }

    private fun opprettBehandleSakOppgaveTask(behandling: Behandling) {
        val payload = OpprettOppgavePayload(behandling.id, SikkerhetContext.hentSaksbehandler())
        val payloadAsString = objectMapper.writeValueAsString(payload)
        val task = LagBehandleSakOppgaveTask.opprettTask(payloadAsString)
        taskService.save(task)
    }

    fun hentBehandling(behandlingId: UUID): Behandling? = behandlingRepository.findByIdOrNull(behandlingId)

    fun hentBehandlingerFraFagsak(fagsakId: UUID): List<Behandling>? = behandlingRepository.findAllByFagsakId(fagsakId)

    fun finnesÅpenBehandling(fagsakId: UUID) = behandlingRepository.existsByFagsakIdAndStatusIsNot(fagsakId, BehandlingStatus.FERDIGSTILT)
}
