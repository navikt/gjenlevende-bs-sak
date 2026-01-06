package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.brev.FamilieDokumentClient
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.brev.lagHtml
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = BrevTask.TYPE,
    maxAntallFeil = 1,
    settTilManuellOppfølgning = true,
    beskrivelse = "Brev task",
)
class BrevTask(
    val brevService: BrevService,
    private val familieDokumentClient: FamilieDokumentClient,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val brev =
            brevService.hentBrev(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId")
        val html = lagHtml(brev.brevJson)
        val pdf = familieDokumentClient.genererPdfFraHtml(html)
        brevService.lagreBrevPdf(behandlingId, pdf)

        logger.info("Gjennomførte BrevTask: behandlingId={} - PDF generert og lagret ({} bytes)", behandlingId, pdf.size)
    }

    companion object {
        const val TYPE = "BrevTask"

        fun opprettTask(payload: String): Task =
            Task(
                TYPE,
                payload,
            )
    }
}
