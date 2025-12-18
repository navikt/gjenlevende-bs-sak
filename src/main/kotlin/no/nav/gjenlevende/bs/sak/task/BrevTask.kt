package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.brev.FamilieDokumentClient
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
@TaskStepBeskrivelse(
    taskStepType = BrevTask.TYPE,
    maxAntallFeil = 1,
    settTilManuellOppfølgning = true,
    beskrivelse = "Brev task",
)
open class BrevTask(
    val brevService: BrevService,
    val objectMapper: ObjectMapper,
    val familieDokumentClient: FamilieDokumentClient,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val brevRequest = objectMapper.readValue(task.payload, BrevRequest::class.java)
        val html = brevService.genererHTMLFraBrevRequest(brevRequest)
        val pdf: ByteArray = familieDokumentClient.genererPdfFraHtml(html)
        println(pdf)
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
