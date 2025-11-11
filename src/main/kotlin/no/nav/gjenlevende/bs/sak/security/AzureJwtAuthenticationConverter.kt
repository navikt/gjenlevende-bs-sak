package no.nav.gjenlevende.bs.sak.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class AzureJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val NAVIDENT_CLAIM = "NAVident"
        private const val GROUPS_CLAIM = "groups"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        // Valider at NAVident finnes
        // TODO: Sjekk om dette er påkrevd, mistenker det er det, men kan være det går fint uten.
        val navIdent = jwt.getClaimAsString(NAVIDENT_CLAIM)

        if (navIdent.isNullOrBlank()) {
            logger.warn("JWT mangler påkrevd claim: $NAVIDENT_CLAIM")
            throw ManglendeClaimException("Token mangler påkrevd claim: $NAVIDENT_CLAIM")
        }

        val grupper = jwt.getClaimAsStringList(GROUPS_CLAIM) ?: emptyList()

        // Map Azure AD grupper til applikasjonsroller
        val roller = Rolle.fraAzureGrupper(grupper)

        if (roller.isEmpty()) {
            logger.warn(
                "Bruker $navIdent har ingen gyldige roller. Grupper i token: ${grupper.joinToString(", ")}",
            )
            throw UtilstrekkeligTilgangException(
                "Bruker har ikke tilgang til applikasjonen. Mangler påkrevde gruppemedlemskap.",
            )
        }

        // Konverter roller til Spring Security authorities
        val authorities = roller.map { SimpleGrantedAuthority(it.authority()) }

        // TODO: Fjern denne, husk.
        logger.info(
            "Autentisert bruker $navIdent med roller: ${roller.joinToString(", ")}",
        )

        return JwtAuthenticationToken(jwt, authorities)
    }
}

class ManglendeClaimException(
    message: String,
) : RuntimeException(message)

class UtilstrekkeligTilgangException(
    message: String,
) : RuntimeException(message)
