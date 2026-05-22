package no.nav.gjenlevende.bs.sak.journalpost

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('SAKSBEHANDLER')")
@RequestMapping("/api/journalpost")
class JournalpostController(
    private val journalpostService: JournalpostService,
) {
    @GetMapping(
        path = ["/{journalpostId}/dokument-pdf/{dokumentInfoId}", "/{journalpostId}/dokument-pdf/{dokumentInfoId}/{filnavn}"],
        produces = [MediaType.APPLICATION_PDF_VALUE],
    )
    fun hentDokumentSomPdf(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
    ): ByteArray = journalpostService.hentDokument(journalpostId, dokumentInfoId)
}
