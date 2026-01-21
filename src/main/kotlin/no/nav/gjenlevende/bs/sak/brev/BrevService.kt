package no.nav.gjenlevende.bs.sak.brev

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.task.BrevTask
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
    fun lagBrevPdfTask(behandlingId: UUID): Task =
        BrevTask.opprettTask(
            objectMapper.writeValueAsString(
                BrevTask.LagBrevPdfTaskData(behandlingId),
            ),
        )

    @Transactional
    fun mellomlagreBrev(
        behandlingId: UUID,
        brevRequest: BrevRequest,
    ) {
        val brev =
            Brev(
                behandlingId = behandlingId,
                brevJson = brevRequest,
            )

        if (brevRepository.existsById(behandlingId)) {
            brevRepository.update(brev)
        } else {
            brevRepository.insert(brev)
        }
    }

    fun hentBrev(behandlingId: UUID): Brev? = brevRepository.findByIdOrNull(behandlingId)

    @Transactional
    fun oppdatereBrevPdf(
        behandlingId: UUID,
        pdf: ByteArray,
    ) {
        val eksisterendeBrev =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved lagring av PDF")
        val oppdatertBrevPdf = eksisterendeBrev.copy(brevPdf = pdf)
        brevRepository.update(oppdatertBrevPdf)
    }

    @Transactional
    fun oppdaterSaksbehandler(
        behandlingId: UUID,
        saksbehandler: String?,
    ) {
        val eksisterendeBrev =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved oppdatering av saksbehandler")
        val oppdatert = eksisterendeBrev.copy(saksbehandler = saksbehandler)
        brevRepository.update(oppdatert)
    }

    @Transactional
    fun oppdaterBeslutter(
        behandlingId: UUID,
        beslutter: String?,
    ) {
        val eksisterendeBrev =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved oppdatering av beslutter")
        val oppdatert = eksisterendeBrev.copy(beslutter = beslutter)
        brevRepository.update(oppdatert)
    }

    fun lagHtml(request: BrevRequest): String {
        val tittel = request.brevmal.tittel
        val navn = request.brevmal.informasjonOmBruker.navn
        val personident = request.brevmal.informasjonOmBruker.fnr
        val logo = logoTilBase64()
        val fritekst = lagHtmlTekstbolker(request.fritekstbolker)
        val avslutning = lagHtmlTekstbolker(request.brevmal.fastTekstAvslutning)
        val saksbehandlerNavn = "saksbehandler navn"
        val saksbehandlerEnhet = "Nav familie- og pensjonsytelser" // TODO hente enhet og appende
        val beslutterNavn = "beslutter navn"
        val beslutterEnhet = "Nav familie- og pensjonsytelser" // TODO hente enhet og appende

        return """
            <!DOCTYPE html>
            <html lang="no">
            <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>$tittel</title>
                <style type="text/css">
                    body { font-family: Arial, Helvetica, sans-serif; font-size: 11pt; line-height: 12pt; margin-left: 48pt; margin-right: 48pt; }
                    header { margin-bottom: 12pt; }
                    h1 { font-size: 16pt; line-height: 20pt; font-weight: 700; margin-bottom: 26pt; }
                    h2 { font-size: 13pt; line-height: 16pt; font-weight: 700; margin-bottom: 6pt;}
                    h3 { font-size: 12pt; line-height: 16pt; font-weight: 700; margin-bottom: 6pt;}
                    section { margin-bottom: 26pt; }
                    .header {
                        padding-top: 32pt;
                        margin-bottom: 48pt
                    }
                    .logo {
                        display: block;
                        margin-bottom: 32pt
                    }
                    .bruker-info { display: table; }
                    .bruker-info .row { display: table-row; }
                    .bruker-info .label {
                        display: table-cell;
                        white-space: nowrap;
                        padding-right: 12pt;
                    }
                    .bruker-info .value {
                        display: table-cell;
                        width: 100%;
                    }
                </style>
            </head>
            <body>
                <header class="header">
                    <img class="logo" src="$logo" alt="Logo" height="16" />
                    <div class="bruker-info">
                        <div class="row"><span class="label">Navn:</span><span class="value">$navn</span></div>
                        <div class="row"><span class="label">Fødselsnummer:</span><span class="value">$personident</span></div>
                    </div>
                </header>
                <main>
                    <h1>$tittel</h1>
                    $fritekst
                    $avslutning
                </main>
                <footer>
                <p> Med vennlig hilsen,</p>
                <div class="bruker-info">
                        <div class="row">
                            <span class="label">$beslutterNavn</span>
                            <span class="value">$saksbehandlerNavn</span>
                        </div>
                        <div class="row">
                            <span class="label">$beslutterEnhet</span>
                            <span class="value">$saksbehandlerEnhet</span>
                        </div>
                    </div>
              </footer>
            </body>
            </html>
            """.trimIndent()
    }
}
