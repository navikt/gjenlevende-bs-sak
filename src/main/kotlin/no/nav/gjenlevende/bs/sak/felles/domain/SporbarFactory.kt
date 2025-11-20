package no.nav.gjenlevende.bs.sak.felles.domain

import no.nav.gjenlevende.bs.sak.security.SecurityContextProvider
import no.nav.gjenlevende.bs.sak.security.TokenPrincipal
import org.springframework.stereotype.Component

@Component
class SporbarFactory(
    private val securityContextProvider: SecurityContextProvider,
) {
    fun opprett(): Sporbar {
        val principal = securityContextProvider.krevPrincipal()
        val (aktorId, aktørType) =
            when (principal) {
                is TokenPrincipal.Bruker -> principal.navIdent to AktørType.BRUKER
                is TokenPrincipal.Applikasjon -> principal.azpNavn to AktørType.SYSTEM
            }

        return Sporbar(
            opprettetAv = aktorId,
            opprettetAvType = aktørType,
            opprettetTid = SporbarUtils.now(),
            endret =
                Endret(
                    endretAv = aktorId,
                    endretAvType = aktørType,
                    endretTid = SporbarUtils.now(),
                ),
        )
    }

    fun opprettEndret(): Endret {
        val principal = securityContextProvider.krevPrincipal()
        val (aktorId, aktorType) =
            when (principal) {
                is TokenPrincipal.Bruker -> principal.navIdent to AktørType.BRUKER
                is TokenPrincipal.Applikasjon -> principal.azpNavn to AktørType.SYSTEM
            }

        return Endret(
            endretAv = aktorId,
            endretAvType = aktorType,
            endretTid = SporbarUtils.now(),
        )
    }
}
