package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev.",
)
class JournalførVedtaksbrevTask(
    private val dokarkivClient: DokarkivClient,
    private val objectMapper: ObjectMapper,
    private val journalføringService: JournalføringService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val taskData = objectMapper.readValue<JournalførVedtaksbrevTaskData>(task.payload)
        val behandlingId = taskData.behandlingId
        val journalføringRequester = journalføringService.lagJournalføringRequester(behandlingId)

        journalføringRequester.forEach { request ->
            val response = dokarkivClient.arkiverDokument(request)
            logger.info("Journalført vedtaksbrev for mottaker ${request.avsenderMottaker?.navn}: $response")
        }
    }

    companion object {
        const val TYPE = "journalførVedtaksbrev"

        fun opprettTask(payload: String): Task =
            Task(
                TYPE,
                payload,
            )
    }

    data class JournalførVedtaksbrevTaskData(
        val behandlingId: UUID,
    )
}
