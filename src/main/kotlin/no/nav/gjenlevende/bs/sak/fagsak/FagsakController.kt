package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.familie.prosessering.rest.Ressurs
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.TilgangService
import no.nav.gjenlevende.bs.sak.infotrygd.InfotrygdClient
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/fagsak"])
@Validated
open class FagsakController(
    private val fagsakService: FagsakService,
    private val tilgangService: TilgangService,
) {
    private val logger = LoggerFactory.getLogger(InfotrygdClient::class.java)

    @PostMapping
    open fun hentEllerOpprettFagsakForPerson(
        @RequestBody fagsakRequest: FagsakRequest,
    ): Ressurs<FagsakDto> {
        logger.info("kaller hentEllerOpprettFagsakForPerson")
        tilgangService.validerTilgangTilPersonMedBarn(fagsakRequest.personIdent)

        return Ressurs.success(
            fagsakService.hentEllerOpprettFagsakMedBehandlinger(
                fagsakRequest.personIdent,
                fagsakRequest.stønadstype,
            ),
        )
    }
}
