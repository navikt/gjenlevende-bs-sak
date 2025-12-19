package no.nav.gjenlevende.bs.sak.brev

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/brev"])
@Tag(name = "Lager brev-task", description = "Lager task for å lage pdf brev")
class BrevController(
    private val brevService: BrevService,
    private val taskService: TaskService,
) {
    @PostMapping("/lag-task/{behandlingsId}")
    @Operation(
        summary = "Lager brev-task",
        description = "Lager task for å lage pdf brev",
    )
    fun lagBrevTask(
        @PathVariable behandlingsId: UUID,
    ): ResponseEntity<String> {
        val task = brevService.lagBrevPDFtask(behandlingsId)
        taskService.save(task)
        return ResponseEntity.ok("OK")
    }

    @PostMapping("/{behandlingsId}")
    fun opprettBrev(
        @PathVariable behandlingsId: UUID,
        @RequestBody brevRequest: BrevRequest,
    ): ResponseEntity<UUID> {
        brevService.opprettBrev(behandlingsId, brevRequest)
        return ResponseEntity.ok(behandlingsId)
    }
}
