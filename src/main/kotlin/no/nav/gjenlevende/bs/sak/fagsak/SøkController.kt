package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class Søkeresultat(
    val navn: String,
    val personident: String,
    val fagsakPersonId: UUID?,
    val harTilgang: Boolean,
    val harFagsak: Boolean,
)

data class SøkRequest(
    val personident: String?,
    val fagsakPersonId: UUID?,
)

const val LENGDE_PERSONIDENT = 11

@RestController
@RequestMapping(path = ["/api/sok"])
class SøkController(
    private val søkService: SøkService,
) {
    @PostMapping("/person")
    fun søkPerson(
        @RequestBody søkRequest: SøkRequest,
    ): Søkeresultat {
        when {
            søkRequest.personident != null -> {
                validerErPersonident(søkRequest.personident)
                return søkService.søkPerson(søkRequest.personident)
            }

            søkRequest.fagsakPersonId != null -> {
                return søkService.søkMedFagsakPersonId(søkRequest.fagsakPersonId)
            }

            else -> {
                throw Feil(
                    message = "Må oppgi enten personident eller fagsakPersonId",
                    frontendFeilmelding = "Må oppgi enten personident eller fagsakPersonId",
                    httpStatus = HttpStatus.BAD_REQUEST,
                )
            }
        }
    }

    private fun validerErPersonident(personident: String) {
        if (personident.isBlank()) {
            throw Feil(
                message = "Personident kan ikke være tom",
                frontendFeilmelding = "Personident må fylles ut",
                httpStatus = HttpStatus.BAD_REQUEST,
            )
        }

        if (personident.length != LENGDE_PERSONIDENT) {
            throw Feil(
                message = "Personident må være $LENGDE_PERSONIDENT siffer, var ${personident.length}",
                frontendFeilmelding = "Personident må være $LENGDE_PERSONIDENT siffer",
                httpStatus = HttpStatus.BAD_REQUEST,
            )
        }

        if (!personident.all { it.isDigit() }) {
            throw Feil(
                message = "Personident kan kun inneholde tall",
                frontendFeilmelding = "Personident kan kun inneholde tall",
                httpStatus = HttpStatus.BAD_REQUEST,
            )
        }
    }
}
