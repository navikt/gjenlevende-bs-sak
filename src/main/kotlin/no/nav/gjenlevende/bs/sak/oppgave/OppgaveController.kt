package no.nav.gjenlevende.bs.sak.oppgave

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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
class OppgaveController(
    private val oppgaveClient: OppgaveClient,
) {
    @Operation(
        summary = "Opprett en ny oppgave",
        description = "Oppretter en ny oppgave",
        parameters = [
            Parameter(
                name = "request",
                description = "Request med personident og oppgavedetaljer",
                example =
                    "{\n" +
                        "  \"personident\": \"yourPersonidentValue\",\n" +
                        "  \"tema\": \"EYO\",\n" +
                        "  \"behandlingstema\": \"ab0028\",\n" +
                        "  \"beskrivelse\": \"Test: vil prøve å opprette oppgave.\",\n" +
                        "  \"oppgavetype\": \"GEN\",\n" +
                        "  \"aktivDato\": \"2026-01-21\",\n" +
                        "  \"prioritet\": \"NORM\"\n" +
                        "}",
            ),
        ],
    )
    @PostMapping("/lagOppgave")
    fun lagOppgave(
        @RequestBody request: LagEnkelTestOppgaveRequest,
    ): Oppgave = oppgaveClient.opprettOppgaveOBO(request).block() ?: throw RuntimeException("Klarte ikke opprette oppgave")
}

data class
LagEnkelTestOppgaveRequest(
    val personident: String,
    val tema: Tema,
    val behandlingstema: String,
    val beskrivelse: String,
    val oppgavetype: OppgavetypeEYO,
    val aktivDato: String,
    val prioritet: OppgavePrioritet,
)
