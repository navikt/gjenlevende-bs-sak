package no.nav.gjenlevende.bs.sak.task

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test/task")
@Tag(name = "Lag dummy task med gitt payload", description = "Lag dummy task med gitt payload")
class DummyTaskController(
    private val taskService: TaskService,
) {
    @GetMapping("/task")
    @Operation(
        summary = "Lag dummytask med gitt payload",
        description = "Lager en dummy task med gitt payload",
    )
    fun lagDummyTask(
        @RequestBody payload: String,
    ) {
        val task = DummyTask.opprettTask(payload)
        taskService.save(task)
    }
}
