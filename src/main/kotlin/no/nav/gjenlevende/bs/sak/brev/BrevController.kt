package no.nav.gjenlevende.bs.sak.brev

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/brev"])
@Tag(name = "Lager brev-task", description = "Lager task for å lage pdf brev")
class BrevController(
    private val brevService: BrevService,
    private val taskService: TaskService,
) {
    @PostMapping("/test")
    @Operation(
        summary = "Lager brev-task",
        description = "Lager task for å lage pdf brev",
    )
    fun lagBrevTask(
        @RequestBody brevRequest: BrevRequest,
    ): ResponseEntity<String> {
        val task = brevService.lagBrevPDFtask(brevRequest)
        taskService.save(task)
        return ResponseEntity.ok("OK")
    }
}

data class BrevRequest(
    val brevmal: BrevmalDto,
    val fritekstBolker: List<TekstbolkDto>,
)

data class BrevmalDto(
    val tittel: String,
    val informasjonOmBruker: InformasjonOmBrukerDto,
    val fastTekstAvslutning: List<TekstbolkDto>,
)

data class InformasjonOmBrukerDto(
    val navn: String,
    val fnr: String,
)

data class TekstbolkDto(
    val underoverskrift: String?,
    val innhold: String,
)
