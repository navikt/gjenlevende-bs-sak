package no.nav.gjenlevende.bs.sak.behandling

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.oppgave.OppgaveService
import no.nav.gjenlevende.bs.sak.util.findByIdOrThrow
import org.slf4j.LoggerFactory
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
    private val taskService: TaskService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val payload: OpprettOppgavePayload = objectMapper.readValue(task.payload)

        logger.info("LagBehandleSakOppgaveTask task: ${payload.behandlingsId}, saksbehandler: ${payload.saksbehandler}")
        val behandling = behandlingRepository.findByIdOrThrow(payload.behandlingsId)
        oppgaveService.opprettBehandleSakOppgave(behandling, payload.saksbehandler)
    }

    fun opprettBehandleSakOppgaveTask(behandling: Behandling, saksbehandler: String) {
        val payload = OpprettOppgavePayload(behandling.id, saksbehandler)
        val payloadAsString = objectMapper.writeValueAsString(payload)
        val task = opprettTask(payloadAsString)
        taskService.save(task)
    }

    companion object {
        const val TYPE = "LagBehandleSakOppgaveTask"

        fun opprettTask(payload: String): Task =
            Task(
                TYPE,
                payload,
            )
    }
}

data class OpprettOppgavePayload(
    val behandlingsId: UUID,
    val saksbehandler: String,
)
