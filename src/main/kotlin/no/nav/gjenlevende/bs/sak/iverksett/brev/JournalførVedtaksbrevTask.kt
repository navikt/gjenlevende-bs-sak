package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.brev.BrevmottakerService
import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker
import no.nav.gjenlevende.bs.sak.brev.domain.MottakerType
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.fagsak.domain.StønadType
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import no.nav.gjenlevende.bs.sak.iverksett.domene.ArkivDokument
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottaker
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottakerIdType
import no.nav.gjenlevende.bs.sak.iverksett.domene.DokarkivBruker
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokument
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumenttype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumentvariant
import no.nav.gjenlevende.bs.sak.iverksett.domene.Fagsystem
import no.nav.gjenlevende.bs.sak.iverksett.domene.Filtype
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.Sak
import no.nav.gjenlevende.bs.sak.iverksett.metadata.tilMetadata
import no.nav.gjenlevende.bs.sak.saf.BrukerIdType
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
    private val behandlingService: BehandlingService,
    private val fagsakRepository: FagsakRepository,
    private val fagsakPersonService: FagsakPersonService,
    private val brevService: BrevService,
    private val brevmottakerService: BrevmottakerService,
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
            // TODO lagre journalpostId og dokumentId fra dokarkivResponse i iverksettResultat
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
