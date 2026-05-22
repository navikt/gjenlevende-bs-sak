package no.nav.gjenlevende.bs.sak.journalpost

import org.springframework.stereotype.Service

@Service
class JournalpostService(
    private val journalpostClient: JournalpostClient,
) {
    fun hentDokument(
        journalpostId: String,
        dokumentInfoId: String,
    ): ByteArray = journalpostClient.hentDokument(journalpostId, dokumentInfoId)
}
