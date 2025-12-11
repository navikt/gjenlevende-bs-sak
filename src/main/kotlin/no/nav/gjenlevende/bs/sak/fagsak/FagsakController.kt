package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.familie.prosessering.rest.Ressurs
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.TilgangService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
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
    @PostMapping
    open fun hentEllerOpprettFagsakForPerson(
        @RequestBody fagsakRequest: FagsakRequest,
    ): Ressurs<FagsakDto> {
        // tilgangService.validerTilgangTilPersonMedBarn(fagsakRequest.personIdent, AuditLoggerEvent.CREATE)
        // logger.info("kaller hentEllerOpprettFagsakForPerson")

        val fagsakDto =
            when {
                fagsakRequest.personIdent != null -> {
                    fagsakService.hentEllerOpprettFagsakMedBehandlinger(
                        fagsakRequest.personIdent,
                        fagsakRequest.stønadstype,
                    )
                }

                fagsakRequest.fagsakPersonId != null -> {
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
