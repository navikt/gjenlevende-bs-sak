package no.nav.gjenlevende.bs.sak.security

import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

/**
 * Konverterer JWT til authentication token med support for både OBO og M2M tokens.
 *
 * OBO (On-Behalf-Of): Tokens med NAVident claim representerer en bruker.
 * M2M (Machine-to-Machine): Tokens med azp_name claim representerer en applikasjon.
 */
@Component
class AzureJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val NAVIDENT_CLAIM = "NAVident"
        private const val AZP_NAME_CLAIM = "azp_name"
        private const val AZP_CLAIM = "azp"
        private const val GROUPS_CLAIM = "groups"
        private const val NAME_CLAIM = "name"
        private const val PREFERRED_USERNAME_CLAIM = "preferred_username"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val tokenPrincipal = utledTokenPrincipal(jwt)
        val authorities =
            utledAuthorities(
                jwt = jwt,
                principal = tokenPrincipal,
            )

        return GjenlevendeJwtAuthenticationToken(
            jwt = jwt,
            tokenPrincipal = tokenPrincipal,
            authorities = authorities,
        )
    }

    private fun utledTokenPrincipal(jwt: Jwt): TokenPrincipal {
        val navIdent = jwt.getClaimAsString(NAVIDENT_CLAIM)
        val azpName = jwt.getClaimAsString(AZP_NAME_CLAIM)

        return when {
            !navIdent.isNullOrBlank() -> {
                TokenPrincipal.Bruker(
                    navIdent = navIdent,
                    navn = jwt.getClaimAsString(NAME_CLAIM),
                    epost = jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM),
                )
            }

            !azpName.isNullOrBlank() -> {
                val clientId =
                    jwt.getClaimAsString(AZP_CLAIM)
                        ?: throw ManglendeClaimException("M2M token mangler 'azp' claim")

                TokenPrincipal.Applikasjon(
                    azpNavn = azpName,
                    clientId = clientId,
                )
            }

            else -> {
                logger.warn("JWT mangler både NAVident og azp_name claims")
                throw ManglendeClaimException("Token må inneholde enten '$NAVIDENT_CLAIM' (bruker) eller '$AZP_NAME_CLAIM' (applikasjon)")
            }
        }
    }

    private fun utledAuthorities(
        jwt: Jwt,
        principal: TokenPrincipal,
    ): Collection<GrantedAuthority> =
        when (principal) {
            is TokenPrincipal.Bruker -> utledBrukerAuthorities(jwt, principal.navIdent)
            is TokenPrincipal.Applikasjon -> utledApplikasjonAuthorities(principal.azpNavn)
        }

    private fun utledBrukerAuthorities(
        jwt: Jwt,
        navIdent: String,
    ): Collection<GrantedAuthority> {
        val grupper = jwt.getClaimAsStringList(GROUPS_CLAIM) ?: emptyList()
        val roller = Rolle.fraAzureGrupper(grupper)

        if (roller.isEmpty()) {
            logger.warn("Bruker $navIdent har ingen gyldige roller. Grupper i token: ${grupper.joinToString(", ")}")
            throw UtilstrekkeligTilgangException("Bruker har ikke tilgang til applikasjonen. Mangler påkrevde gruppemedlemskap.")
        }

        return roller.map { SimpleGrantedAuthority(it.authority()) }
    }

    private fun utledApplikasjonAuthorities(azpNavn: String): Collection<GrantedAuthority> {
        logger.info("M2M applikasjon $azpNavn får standard application authorities")
        return listOf(SimpleGrantedAuthority("ROLE_APPLICATION"))
    }
}

class ManglendeClaimException(
    message: String,
) : RuntimeException(message)

class UtilstrekkeligTilgangException(
    message: String,
) : RuntimeException(message)
