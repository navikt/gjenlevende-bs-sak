package no.nav.gjenlevende.bs.sak.pdl

import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
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
        val data: HentPersonData =
            pdlClient.utførQuery(
                query = graphqlQuery("/pdl/hent_navn.graphql"),
                variables = mapOf("ident" to ident),
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

    fun hentBarnPersonidenter(personident: String): List<String> =
        hentFamilieRelasjoner(personident)
            ?.filter { it.relatertPersonsRolle == Familierolle.BARN }
            ?.mapNotNull { it.relatertPersonsIdent }
            ?: emptyList()

    fun hentForeldrePersonidenter(personident: String): List<String> =
        hentFamilieRelasjoner(personident)
            ?.filter { it.relatertPersonsRolle in listOf(Familierolle.FAR, Familierolle.MOR, Familierolle.MEDMOR) }
            ?.mapNotNull { it.relatertPersonsIdent }
            ?: emptyList()

    private fun hentFamilieRelasjoner(personident: String): List<ForelderBarnRelasjon>? {
        val data: FamilieRelasjonerResponse =
            pdlClient.utførQuery(
                query = graphqlQuery("/pdl/hent_familie_relasjoner.graphql"),
                variables = mapOf("ident" to personident),
                operasjon = "hentFamilieRelasjoner",
            ) ?: return null

        return data.hentPerson?.forelderBarnRelasjon
    }

    fun graphqlQuery(path: String) =
        PdlService::class.java
            .getResource(path)
            ?.readText()
            ?.graphqlCompatible() ?: throw PdlException("Kunne ikke lese graphql query fra path: $path")

    private fun String.graphqlCompatible(): String = StringUtils.normalizeSpace(this.replace("\n", ""))
}
