package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.TilgangService
import no.nav.gjenlevende.bs.sak.pdl.Navn
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SøkService(
    private val fagsakPersonService: FagsakPersonService,
    private val pdlService: PdlService,
    private val tilgangService: TilgangService,
) {
    fun søkPerson(personident: String): Søkeresultat {
        tilgangService.validerTilgangTilPersonMedRelasjoner(personident)
        val fagsakPerson = fagsakPersonService.finnPerson(setOf(personident))

        if (fagsakPerson == null) {
            return tilSøkeresultat(personident, null, null)
        }

        val navnPdl = pdlService.hentNavn(fagsakPerson.id)

        return tilSøkeresultat(personident, fagsakPerson, navnPdl)
    }

    fun søkMedFagsakPersonId(fagsakPersonId: UUID): Søkeresultat {
        val fagsakPerson = fagsakPersonService.finnPersonMedId(fagsakPersonId)

        if (fagsakPerson == null) {
            return tilSøkeresultat("Ukjent", null, null)
        }

        val personident = fagsakPersonService.hentAktivIdent(fagsakPersonId)
        tilgangService.validerTilgangTilPersonMedRelasjoner(personident)

        val navnPdl = pdlService.hentNavn(fagsakPersonId)

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
