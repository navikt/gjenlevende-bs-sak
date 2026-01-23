package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

sealed class TokenInfo {
    abstract val tokenValue: String

    data class BrukerToken(
        val navIdent: String,
        override val tokenValue: String,
    ) : TokenInfo()

    data class ApplikasjonToken(
        val applikasjonNavn: String,
        val applikasjonId: String,
        override val tokenValue: String,
    ) : TokenInfo()
}

object SikkerhetContext {
    private const val SYSTEM_FORKORTELSE = "VL"
    private const val NAVIDENT_CLAIM = "NAVident"
    private const val IDTYP_CLAIM = "idtyp"
    private const val IDTYP_APP = "app"
    private const val AZP_NAME_CLAIM = "azp_name"
    private const val AZP_CLAIM = "azp"

    fun hentTokenInfo(): TokenInfo {
        val authentication = SecurityContextHolder.getContext().authentication ?: throw IllegalStateException("Ingen authentication i SecurityContext")

        if (authentication !is JwtAuthenticationToken) {
            throw IllegalStateException("Authentication er ikke JwtAuthenticationToken")
        }

        val jwt = authentication.token
        val idtyp = jwt.getClaimAsString(IDTYP_CLAIM)

        return when (idtyp) {
            IDTYP_APP -> {
                val azpName = jwt.getClaimAsString(AZP_NAME_CLAIM) ?: throw IllegalStateException("Applikasjonstoken mangler azp_name claim")
                val azp = jwt.getClaimAsString(AZP_CLAIM) ?: throw IllegalStateException("Applikasjonstoken mangler azp claim")

                TokenInfo.ApplikasjonToken(
                    applikasjonNavn = azpName,
                    applikasjonId = azp,
                    tokenValue = jwt.tokenValue,
                )
            }

            else -> {
                val navIdent = jwt.getClaimAsString(NAVIDENT_CLAIM) ?: throw IllegalStateException("Brukertoken mangler NAVident claim")

                TokenInfo.BrukerToken(
                    navIdent = navIdent,
                    tokenValue = jwt.tokenValue,
                )
            }
        }
    }

    fun erMaskinTilMaskinToken(): Boolean = hentTokenInfo() is TokenInfo.ApplikasjonToken

    fun hentSaksbehandler(): String =
        when (val tokenInfo = hentTokenInfo()) {
            is TokenInfo.BrukerToken -> tokenInfo.navIdent

            is TokenInfo.ApplikasjonToken -> throw IllegalStateException(
                "Forventet brukertoken, men fikk applikasjonstoken fra ${tokenInfo.applikasjonNavn}",
            )
        }

    fun hentSaksbehandlerEllerSystembruker(): String {
        val authentication = SecurityContextHolder.getContext().authentication ?: return SYSTEM_FORKORTELSE

        if (authentication !is JwtAuthenticationToken) {
            return SYSTEM_FORKORTELSE
        }

        val tokenInfo = hentTokenInfo()

        return when (tokenInfo) {
            is TokenInfo.BrukerToken -> tokenInfo.navIdent
            is TokenInfo.ApplikasjonToken -> SYSTEM_FORKORTELSE
        }
    }

    fun hentBrukerToken(): String {
        val tokenInfo = hentTokenInfo()

        return when (tokenInfo) {
            is TokenInfo.BrukerToken -> tokenInfo.tokenValue
            is TokenInfo.ApplikasjonToken -> throw IllegalStateException("Forventet brukertoken, men fikk applikasjonstoken fra ${tokenInfo.applikasjonNavn}")
        }
    }
}
