package no.nav.gjenlevende.bs.sak.task

import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class DummyTaskService(
    private val taskService: TaskService,
) {
    @Transactional
    open fun opprettDummyTask(payload: String) {
        val task = DummyTask.opprettTask(payload)
        taskService.save(task)
    }
}
