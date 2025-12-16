package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = BrevTask.TYPE,
    maxAntallFeil = 1,
    settTilManuellOppfølgning = true,
    beskrivelse = "Brev task",
)
open class BrevTask : AsyncTaskStep {
    override fun doTask(task: Task) {
        println("Lage brev her: ${task.payload}") // TODO: Implementer brevlogikk
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
