package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.PersonIdent
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.feilHvisIkke
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
open class FagsakPersonService(
    private val fagsakPersonRepository: FagsakPersonRepository,
) {
    @Transactional
    open fun hentEllerOpprettPerson(
        personIdenter: Set<String>,
        gjeldendePersonIdent: String,
    ): FagsakPerson {
        feilHvisIkke(personIdenter.contains(gjeldendePersonIdent)) {
            "Liste med personidenter inneholder ikke gjeldende personident"
        }
        return (
            fagsakPersonRepository.findByIdent(personIdenter)
                ?: opprettFagsakPerson(gjeldendePersonIdent)
        )
    }

    fun opprettFagsakPerson(gjeldendePersonIdent: String): FagsakPerson = fagsakPersonRepository.insert(FagsakPerson(identer = setOf(PersonIdent(gjeldendePersonIdent))))

    fun hentAktivIdent(fagsakPersonId: UUID): String {
        val fagsakPerson =
            fagsakPersonRepository
                .findById(fagsakPersonId)
                .orElseThrow { IllegalArgumentException("Fant ingen fagsakPerson med id $fagsakPersonId") }

        return fagsakPerson.identer.maxByOrNull { it.sporbar.endret.endretTid }?.ident
            ?: throw IllegalStateException("FagsakPerson har ingen identer")
    }
}
