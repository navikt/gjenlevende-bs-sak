package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.iverksett.domene.DistribuerJournalpostRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.Distribusjonstype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Fagsystem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.UUID

@TaskStepBeskrivelse(
    taskStepType = DistribuerVedtaksbrevTask.TYPE,
    maxAntallFeil = DistribuerVedtaksbrevTask.MAX_FORSØK,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Distribuerer vedtaksbrev.",
)
@Service
class DistribuerVedtaksbrevTask(
    private val objectMapper: ObjectMapper,
    private val journalpostForBehandlingService: JournalpostForBehandlingService,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private sealed class Resultat

    override fun doTask(task: Task) {
        val taskData = objectMapper.readValue<DistribuerVedtaksbrevTaskData>(task.payload)
        val behandlingId = taskData.behandlingId
        distribuerVedtaksbrev(behandlingId)
    }

    private fun distribuerVedtaksbrev(behandlingId: UUID): Resultat {
        val journalpostIDerSomSkalDistribueres = journalpostForBehandlingService.hentJournalpostIder(behandlingId)
        val noe =
            journalpostIDerSomSkalDistribueres.map {
                DistribuerJournalpostRequest(
                    journalpostId = it,
                    bestillendeFagsystem = Fagsystem.EY,
                    dokumentProdApp = "GJENLEVENDE_BS_SAK",
                    distribusjonstype = Distribusjonstype.VEDTAK,
                )
            }
    }

    companion object {
        const val TYPE = "distribuerVedtaksbrev"
        const val MAX_FORSØK = 50
    }
}

data class DistribuerVedtaksbrevTaskData(
    val behandlingId: UUID,
)
