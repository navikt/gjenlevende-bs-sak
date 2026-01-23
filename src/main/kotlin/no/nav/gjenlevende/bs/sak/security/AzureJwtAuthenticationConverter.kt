package no.nav.gjenlevende.bs.sak.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
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
        private const val IDTYP_CLAIM = "idtyp"
        private const val IDTYP_APP = "app"
        private const val AZP_NAME_CLAIM = "azp_name"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val idtyp = jwt.getClaimAsString(IDTYP_CLAIM)

        return when (idtyp) {
            IDTYP_APP -> konverterApplikasjonToken(jwt)
            else -> konverterBrukerToken(jwt)
        }
    }

    private fun konverterApplikasjonToken(jwt: Jwt): JwtAuthenticationToken {
        val azpName = jwt.getClaimAsString(AZP_NAME_CLAIM) ?: "ukjent-app"
        logger.info("Autentisert applikasjon: $azpName")
        return JwtAuthenticationToken(jwt, listOf(SimpleGrantedAuthority(Rolle.SYSTEM.authority())))
    }

    private fun konverterBrukerToken(jwt: Jwt): JwtAuthenticationToken {
        val navIdent = jwt.getClaimAsString(NAVIDENT_CLAIM)

        if (navIdent.isNullOrBlank()) {
            logger.warn("JWT mangler påkrevd claim: $NAVIDENT_CLAIM")
            throw ManglendeClaimException("Token mangler påkrevd claim: $NAVIDENT_CLAIM")
        }

        val grupper = jwt.getClaimAsStringList(GROUPS_CLAIM) ?: emptyList()

        val roller = Rolle.fraAzureGrupper(grupper)

        if (roller.isEmpty()) {
            logger.warn("Bruker $navIdent har ingen gyldige roller. Grupper i token: ${grupper.joinToString(", ")}")
            throw UtilstrekkeligTilgangException("Bruker har ikke tilgang til applikasjonen. Mangler påkrevde gruppemedlemskap.")
        }

        val authorities = roller.map { SimpleGrantedAuthority(it.authority()) }

        logger.info("Autentisert bruker $navIdent med roller: ${roller.joinToString(", ")}")

        return JwtAuthenticationToken(jwt, authorities)
    }
}

class ManglendeClaimException(
    message: String,
) : RuntimeException(message)

class UtilstrekkeligTilgangException(
    message: String,
) : RuntimeException(message)
