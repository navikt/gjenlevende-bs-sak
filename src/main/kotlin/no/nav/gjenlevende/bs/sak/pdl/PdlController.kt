package no.nav.gjenlevende.bs.sak.pdl

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pdl")
class PdlController(
    private val pdlClient: PdlClient,
) {
    @GetMapping("/navn/{ident}")
    fun hentNavn(
        @PathVariable ident: String,
    ): ResponseEntity<Navn> {
        val navn =
            pdlClient.hentNavn(ident)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(navn)
    }
}
