package no.nav.gjenlevende.bs.sak.brev

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.task.BrevTask
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class BrevService(
    private val objectMapper: ObjectMapper,
) {
    fun lagBrevPDFtask(brevRequest: BrevRequest): Task {
        val payload = objectMapper.writeValueAsString(brevRequest)
        return BrevTask.opprettTask(payload)
    }

    fun genererHTMLFraBrevRequest(brevRequest: BrevRequest): String {
        val sb = StringBuilder()
        sb.append("<html><body>")
        sb.append("<h1>${brevRequest.brevmal.tittel}</h1>")
        sb.append("<h2>Informasjon om bruker</h2>")
        sb.append("<p>Navn: ${brevRequest.brevmal.informasjonOmBruker.navn}</p>")
        sb.append("<p>Fødselsnummer: ${brevRequest.brevmal.informasjonOmBruker.fnr}</p>")
        brevRequest.fritekstbolker.forEach { tekstbolk ->
            tekstbolk.underoverskrift?.let {
                sb.append("<h3>$it</h3>")
            }
            sb.append("<p>${tekstbolk.innhold}</p>")
        }
        brevRequest.brevmal.fastTekstAvslutning.forEach { tekstbolk ->
            tekstbolk.underoverskrift?.let {
                sb.append("<h3>$it</h3>")
            }
            sb.append("<p>${tekstbolk.innhold}</p>")
        }
        sb.append("</body></html>")
        return sb.toString()
    }
}
