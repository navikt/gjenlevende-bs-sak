package no.nav.gjenlevende.bs.sak.barn

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.pdl.PdlException
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BarnService(
    private val pdlService: PdlService,
    private val behandlingService: BehandlingService,
    private val barnRepository: BarnRepository,
    private val fagsakRepository: FagsakRepository,
) {
    fun lagreBarn(request: LagreBarnRequest): List<BehandlingBarn> {
        val behandlingBarn =
            request.barn.map { barn ->
                BehandlingBarn(
                    behandlingId = request.behandlingId,
                    personIdent = barn.personIdent,
                    navn = barn.navn,
                    fødselDato = barn.fødselsdato,
                    hentetTidspunkt = barn.hentetTidspunkt,
                )
            }
        return barnRepository.insertAll(behandlingBarn)
    }

    fun hentTilknyttetBarn(
        request: HentBarnRequest,
    ): List<HentBarnResponse> {
        val fagsak = fagsakRepository.findByFagsakPersonIdAndStønadstype(request.fagsakPersonId, request.stønadstype)
        val behandling = fagsak?.let { behandlingService.finnSisteInnvilgetFerdigstiltBehandling(it.id) }
        val barnPersonIdenter = pdlService.hentBarnPersonidenter(request.personIdent)

        val pdlBarn =
            barnPersonIdenter.map { personIdent ->
                val person =
                    pdlService.hentPersonMedPersonIdent(personIdent)
                        ?: throw PdlException("Kunne ikke hente navn for barn med ident $personIdent")
                HentBarnResponse(personIdent = personIdent, navn = listOfNotNull(person.navn.fornavn, person.navn.mellomnavn, person.navn.etternavn).joinToString(" "), fødselsdato = person.foedselsdato, hentetTidspunkt = LocalDateTime.now())
            }

        val behandlingBarn = behandling?.let { barnRepository.findByBehandlingId(it.id) } ?: emptyList()

        val behandlingBarnResponses =
            behandlingBarn.map { barn ->
                HentBarnResponse(
                    personIdent = barn.personIdent,
                    navn = barn.navn,
                    fødselsdato = barn.fødselDato,
                    hentetTidspunkt = LocalDateTime.now(),
                )
            }

        return (pdlBarn + behandlingBarnResponses)
            .distinctBy { it.personIdent }
    }
}
