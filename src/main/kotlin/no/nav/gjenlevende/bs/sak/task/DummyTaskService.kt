package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
class DummyTaskService(
    private val taskService: TaskService,
) {
    fun opprettDummyTask(payload: String) {
        val task = DummyTask.opprettTask(payload)
        taskService.save(task)
    }
}
