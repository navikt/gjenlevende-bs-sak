package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.familie.prosessering.rest.Ressurs
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.TilgangService
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
    private val fagsakPersonService: FagsakPersonService,
    private val tilgangService: TilgangService,
) {
    private val logger = LoggerFactory.getLogger(FagsakController::class.java)

    @PostMapping
    open fun hentEllerOpprettFagsakForPerson(
        @RequestBody fagsakRequest: FagsakRequest,
    ): Ressurs<FagsakDto> {
        logger.info("kaller hentEllerOpprettFagsakForPerson")

        val fagsakDto =
            when {
                fagsakRequest.personident != null -> {
                    tilgangService.validerTilgangTilPersonMedBarn(fagsakRequest.personident)
                    fagsakService.hentEllerOpprettFagsakMedBehandlinger(
                        fagsakRequest.personident,
                        fagsakRequest.stønadstype,
                    )
                }

                fagsakRequest.fagsakPersonId != null -> {
                    val personident = fagsakPersonService.hentAktivIdent(fagsakRequest.fagsakPersonId)
                    tilgangService.validerTilgangTilPersonMedBarn(personident)
                    fagsakService.hentEllerOpprettFagsakMedFagsakPersonId(
                        fagsakRequest.fagsakPersonId,
                        fagsakRequest.stønadstype,
                    )
                }

                else -> {
                    throw IllegalArgumentException("Må oppgi enten personIdent eller fagsakPersonId")
                }
            }

        return Ressurs.success(fagsakDto)
    }
}
