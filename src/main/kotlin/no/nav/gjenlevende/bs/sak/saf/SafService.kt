package no.nav.gjenlevende.bs.sak.saf

import no.nav.gjenlevende.bs.sak.pdl.PdlException
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service

@Service
class SafService(
    private val safClient: SafClient,
) {
    private val logger = LoggerFactory.getLogger(PdlService::class.java)

    fun hentJournalposterForIdent(brukerId: String): String? {
        val data =
            safClient.utførQuery(
                query = SafConfig.hentJournalposterBrukerQuery,
                variables = mapOf("brukerId" to brukerId, "antall" to "1"),
                responstype = object : ParameterizedTypeReference<SafJournalpostResponse<Journalpost>>() {},
                operasjon = "hentJournalposterForBrukerId",
            ) ?: throw PdlException("Fant ingen person i SAF for brukerId")

        val journalPost =
            data.journalpostId
                ?: throw PdlException("Fant ingen person i SAF")

        val journalPostTittel = data.tittel

        return journalPostTittel
    }
}
