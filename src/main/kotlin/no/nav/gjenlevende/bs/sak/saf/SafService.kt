package no.nav.gjenlevende.bs.sak.saf

import no.nav.gjenlevende.bs.sak.config.SafConfig
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SafService(
    private val safClient: SafClient,
    private val fagsakPersonService: FagsakPersonService
) {
    private val logger = LoggerFactory.getLogger(PdlService::class.java)

    fun hentJournalposterForIdent(fagsakPersonId: UUID): List<Journalpost>? {
        val ident = fagsakPersonService.hentAktivIdent(fagsakPersonId)
        val data =
            safClient.utførQuery(
                query = SafConfig.hentJournalposterBrukerQuery,
                variables = JournalposterForBrukerRequest(Bruker(ident, BrukerIdType.FNR), emptyList(), emptyList(), 10),
                responstype = object : ParameterizedTypeReference<SafJournalpostResponse<SafJournalpostBrukerData>>() {},
                operasjon = "hentJournalposterForBrukerId",
            ) ?: throw SafException("Fant ingen person i SAF for brukerId")

        return data.dokumentoversiktBruker.journalposter
    }
}
