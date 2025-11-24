package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = DummyTask.TYPE,
    maxAntallFeil = 1,
    settTilManuellOppfølgning = true,
    beskrivelse = "Dummy task",
)
open class DummyTask : AsyncTaskStep {
    override fun doTask(task: Task) {
        println("Dummy task: ${task.payload}")
    }

    companion object {
        const val TYPE = "DummyTask"

        fun opprettTask(payload: String): Task =
            Task(
                type = TYPE,
                payload = payload,
            )
    }
}
