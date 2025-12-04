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

    fun hentJournalposterForIdent(fnr: String): List<Journalpost> {
        val data =
            safClient.utførQuery(
                query = SafConfig.hentJournalposterBrukerQuery,
                variables = JournalposterForBrukerRequest(Bruker(fnr, BrukerIdType.FNR), 10),
                responstype = object : ParameterizedTypeReference<SafJournalpostResponse<SafJournalposterData>>() {},
                operasjon = "hentJournalposterForBrukerId",
            ) ?: throw PdlException("Fant ingen person i SAF for brukerId")

        return data.journalposter
    }
}
