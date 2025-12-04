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
    @PostMapping("/tittel")
    fun hentJournalPostForBrukerId(
        @RequestBody request: String,
    ): ResponseEntity<SafJournalposterData> {
        val data =
            safService.hentJournalposterForIdent(request)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(data)
    }
}
