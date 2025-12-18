package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.brev.FamilieDokumentClient
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.UUID

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
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingsId = UUID.fromString(task.payload)
        val brev = brevService.hentBrev(behandlingsId)
        val brevSomJson = objectMapper.readValue(brev?.brevJson, BrevRequest::class.java)
        // TODO generer html, send til familiedokument og lagre respons i brevrepository
        println(brevSomJson)
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
