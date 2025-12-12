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
    @PostMapping("/dokumenter")
    fun hentJournalPostForBrukerId(
        @RequestBody request: HentDokumenterRequest,
    ): ResponseEntity<List<DokumentinfoDto>> {
        val data =
            safService.finnVedleggForPerson(request.fagsakPersonId)

        return ResponseEntity.ok(data)
    }
}
