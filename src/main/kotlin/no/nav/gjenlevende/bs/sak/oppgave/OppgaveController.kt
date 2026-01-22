package no.nav.gjenlevende.bs.sak.oppgave

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.infotrygd.dto.PersonidentRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test/oppgave")
@Tag(
    name = "Oppgave integrasjoonstest",
    description = "Endepunkter for å teste integrasjon mot oppgave",
)
class OppgaveController (private val oppgaveClient: OppgaveClient,){

    @PostMapping("/lagOppgave")
    fun lagOppgave(
        @RequestBody request: PersonidentRequest,
        @AuthenticationPrincipal jwt: Jwt,): Long {

        val oppgave = Oppgave(personident = request.personident,
            tema = Tema.ENF,
            tildeltEnhetsnr = "4489", // TODO finn enhetsnummer for BARNETILSYN GJENLEVENDE 4817 4806 ??? 4817
            behandlingstema = "ab0028",
            beskrivelse = "Henvendelse - teste vil prøve å opprette oppgave.",
            oppgavetype = Oppgavetype.VurderKonsekvensForYtelse.value,
        )

        val oppgaveOpprettet = oppgaveClient.opprettOppgaveOBO(oppgave, jwt.tokenValue)

        return oppgaveOpprettet.id!!
    }


}
