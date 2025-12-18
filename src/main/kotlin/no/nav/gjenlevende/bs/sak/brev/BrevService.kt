package no.nav.gjenlevende.bs.sak.brev

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.task.BrevTask
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class BrevService(
    private val brevRepository: BrevRepository,
    private val objectMapper: ObjectMapper,
) {
    fun lagBrevPDFtask(behandlingsId: UUID): Task = BrevTask.opprettTask(behandlingsId.toString())

    @Transactional
    fun opprettBrev(
        behandlingsId: UUID,
        brevRequest: BrevRequest,
    ): Brev {
        val brev =
            Brev(
                behandlingsId = behandlingsId,
                brevJson = objectMapper.writeValueAsString(brevRequest),
            )

        if (brevRepository.existsById(behandlingsId)) {
            brevRepository.update(brev)
        } else {
            brevRepository.insert(brev)
        }

        return brev
    }

    fun hentBrev(behandlingsId: UUID): Brev? = brevRepository.findByIdOrNull(behandlingsId)
// TODO
//    fun genererHTMLFraBrevRequest(brevRequest: BrevRequest): String {
//        val sb = StringBuilder()
//        sb.append("<html><body>")
//        sb.append("<h1>${brevRequest.brevmal.tittel}</h1>")
//        sb.append("<h2>Informasjon om bruker</h2>")
//        sb.append("<p>Navn: ${brevRequest.brevmal.informasjonOmBruker.navn}</p>")
//        sb.append("<p>Fødselsnummer: ${brevRequest.brevmal.informasjonOmBruker.fnr}</p>")
//        brevRequest.fritekstbolker.forEach { tekstbolk ->
//            tekstbolk.underoverskrift?.let {
//                sb.append("<h3>$it</h3>")
//            }
//            sb.append("<p>${tekstbolk.innhold}</p>")
//        }
//        brevRequest.brevmal.fastTekstAvslutning.forEach { tekstbolk ->
//            tekstbolk.underoverskrift?.let {
//                sb.append("<h3>$it</h3>")
//            }
//            sb.append("<p>${tekstbolk.innhold}</p>")
//        }
//        sb.append("</body></html>")
//        return sb.toString()
//    }
}
