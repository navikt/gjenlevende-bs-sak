package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@Tilgangskontroll
@RequestMapping(path = ["/api/fagsak"])
open class FagsakController(
    private val fagsakService: FagsakService,
) {
    private val logger = LoggerFactory.getLogger(FagsakController::class.java)

    @PostMapping
    fun hentEllerOpprettFagsakForPerson(
        @RequestBody request: FagsakRequest,
    ): ResponseEntity<FagsakDto> {
        logger.info("Kaller hentEllerOpprettFagsakForPerson")
        return ResponseEntity.ok(fagsakService.hentEllerOpprettFagsak(request))
    }
}
