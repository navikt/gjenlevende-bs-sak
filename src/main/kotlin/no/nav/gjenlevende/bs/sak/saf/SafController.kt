package no.nav.gjenlevende.bs.sak.saf

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/saf")
class SafController(
    private val safService: SafService,
) {
    @PostMapping("/journalposter")
    fun hentJournalPostForBrukerId(
        @RequestBody request: HentJournalposterRequest,
    ): ResponseEntity<List<Journalpost>> {
        val data =
            safService.hentJournalposterForIdent(request.fagsakPersonId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(data)
    }
}
