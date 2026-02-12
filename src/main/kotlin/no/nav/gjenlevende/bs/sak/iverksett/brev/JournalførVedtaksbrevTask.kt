package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev.",
)
class JournalførVedtaksbrevTask(
    private val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
    }

    override fun onCompletion(task: Task) {
//        taskService.save(task.opprettNesteTask())
    }

    companion object {
        const val TYPE = "journalførVedtaksbrev"
    }
}
