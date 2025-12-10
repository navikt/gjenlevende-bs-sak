package no.nav.gjenlevende.bs.sak.pdl

import no.nav.gjenlevende.bs.sak.config.PdlConfig
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PdlService(
    private val pdlClient: PdlClient,
    private val fagsakPersonService: FagsakPersonService,
) {
    private val logger = LoggerFactory.getLogger(PdlService::class.java)

    fun hentNavn(fagsakPersonId: UUID): Navn? {
        val ident = fagsakPersonService.hentAktivIdent(fagsakPersonId)
        val data =
            pdlClient.utførQuery(
                query = PdlConfig.hentNavnQuery,
                variables = mapOf("ident" to ident),
                responstype = object : ParameterizedTypeReference<PdlResponse<HentPersonData>>() {},
                operasjon = "hentNavn",
            ) ?: throw PdlException("Fant ingen person i PDL for ident")

        val hentPerson =
            data.hentPerson
                ?: throw PdlException("Fant ingen person i PDL")

        val navnListe = hentPerson.navn
        if (navnListe.isEmpty()) {
            logger.warn("Personen har ingen navn registrert i PDL")
            return null
        }

        return navnListe.first()
    }
}
