package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.pdl.Navn
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import org.springframework.stereotype.Service

@Service
class SøkService(
    private val fagsakPersonService: FagsakPersonService,
    private val pdlService: PdlService,
) {
    fun søkPerson(personident: String): Søkeresultat {
        val fagsakPerson = fagsakPersonService.finnPerson(setOf(personident))

        if (fagsakPerson == null) {
            return tilSøkeresultat(personident, null, null)
        }

        val navnPdl = pdlService.hentNavn(fagsakPerson.id)

        return tilSøkeresultat(personident, fagsakPerson, navnPdl)
    }

    private fun tilSøkeresultat(
        personident: String,
        fagsakPerson: FagsakPerson?,
        navnPdl: Navn?,
    ): Søkeresultat =
        Søkeresultat(
            navn = navnPdl?.let { "${it.fornavn} ${it.etternavn}" } ?: "Ukjent navn",
            personident = personident,
            fagsakPersonId = fagsakPerson?.id,
            harTilgang = true, // TODO: implementer tilgangskontroll senere
            harFagsak = fagsakPerson != null,
        )
}
