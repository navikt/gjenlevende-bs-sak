package no.nav.gjenlevende.bs.sak.iverksett

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Tilgangskontroll
@RequestMapping(path = ["/api/iverksett"])
@Tag(name = "iverksettVedtaksbrevController", description = "Endepunkt for utvikling") // TODO slett
class IverksettVedtaksbrevController(
    private val taskService: TaskService,
    private val iverksettVedtaksbrevService: IverksettVedtaksbrevService,
) {
    @PostMapping("/lag-iverksettVedtaksbrevController-task/{behandlingId}")
    @PreAuthorize("hasRole('ATTESTERING')")
    @Operation(
        summary = "Oppretter brev-task",
        description = "Lager task som genererer pdf-brev",
    )
    fun lagIverksettVedtaksbrevTask(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<String> {
        val task = iverksettVedtaksbrevService.opprettIverksettVedtaksbrevTask(behandlingId)
        taskService.save(task)

        return ResponseEntity.ok("OK")
    }
}
