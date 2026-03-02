package no.nav.gjenlevende.bs.sak.pdl

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pdl")
class PdlController(
    private val pdlService: PdlService,
) {
    @PostMapping("/navn")
    fun hentNavn(
        @RequestBody request: HentNavnRequest,
    ): ResponseEntity<Navn> {
        val navn =
            pdlService.hentNavnMedFagsakPersonId(request.fagsakPersonId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(navn)
    }

    @PostMapping("/barn")
    fun hentBarn(
        @RequestBody request: HentBarnRequest,
    ): ResponseEntity<List<HentBarnResponse>> {
        val barnPersonIdenter = pdlService.hentBarnPersonidenter(request.personIdent)

        val barn =
            barnPersonIdenter.map { personIdent ->
                val navn =
                    pdlService.hentNavnMedPersonident(personIdent)
                        ?: throw PdlException("Kunne ikke hente navn for barn med ident $personIdent")
                HentBarnResponse(personIdent = personIdent, navn = navn)
            }

        return ResponseEntity.ok(barn)
    }
}
