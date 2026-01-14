package no.nav.gjenlevende.bs.sak.behandling

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.oppgave.OppgaveService
import no.nav.gjenlevende.bs.sak.util.findByIdOrThrow
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = LagBehandleSakOppgaveTask.TYPE,
    maxAntallFeil = 3,
    settTilManuellOppfølgning = true,
    beskrivelse = "Oppretter behandle sak oppgave for behandling",
)
class LagBehandleSakOppgaveTask(
    private val behandlingRepository: BehandlingRepository,
    private val oppgaveService: OppgaveService,
    private val objectMapper: ObjectMapper,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val payload: OpprettOppgavePayload = objectMapper.readValue(task.payload)

        println("Dummy task: ${payload.behandlingsId}, saksbehandler: ${payload.saksbehandler}")
        val behandling = behandlingRepository.findByIdOrThrow(payload.behandlingsId)
        oppgaveService.opprettBehandleSakOppgave(behandling, payload.saksbehandler)
    }

    companion object {
        const val TYPE = "LagBehandleSakOppgaveTask"

        fun opprettTask(
            behandlingsId: UUID,
            saksbehandler: String,
        ): Task {
            val payload = OpprettOppgavePayload(behandlingsId, saksbehandler)
            return Task(
                type = TYPE,
                payload = payload.toString(),
            )
        }
    }
}

data class OpprettOppgavePayload(
    val behandlingsId: UUID,
    val saksbehandler: String,
)
