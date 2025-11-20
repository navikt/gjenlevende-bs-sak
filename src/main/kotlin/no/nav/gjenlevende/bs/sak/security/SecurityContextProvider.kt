package no.nav.gjenlevende.bs.sak.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

interface SecurityContextProvider {
    fun hentPrincipal(): TokenPrincipal?

    fun krevBrukerPrincipal(): TokenPrincipal.Bruker

    fun krevPrincipal(): TokenPrincipal
}

@Component
class SpringSecurityContextProvider : SecurityContextProvider {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun hentPrincipal(): TokenPrincipal? {
        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: return null

        return when (authentication) {
            is JwtAuthenticationToken -> {
                authentication.principal as? TokenPrincipal
            }

            else -> {
                logger.warn("Ukjent authentication type: ${authentication::class.simpleName}")
                null
            }
        }
    }

    override fun krevBrukerPrincipal(): TokenPrincipal.Bruker {
        val principal =
            hentPrincipal()
                ?: throw IllegalStateException("Ingen sikkerhetskontekst tilgjengelig")

        return when (principal) {
            is TokenPrincipal.Bruker -> {
                principal
            }

            is TokenPrincipal.Applikasjon -> {
                throw IllegalStateException("Denne operasjonen krever brukerprincipal, men fant applikasjonsprincipal: ${principal.azpNavn}")
            }
        }
    }

    override fun krevPrincipal(): TokenPrincipal =
        hentPrincipal()
            ?: throw IllegalStateException("Ingen sikkerhetskontekst tilgjengelig")
}
