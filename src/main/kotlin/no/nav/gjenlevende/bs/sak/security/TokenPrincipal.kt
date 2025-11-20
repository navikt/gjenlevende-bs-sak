package no.nav.gjenlevende.bs.sak.security

sealed class TokenPrincipal {
    data class Bruker(
        val navIdent: String,
        val navn: String? = null,
        val epost: String? = null,
    ) : TokenPrincipal() {
        init {
            require(navIdent.isNotBlank()) { "NAVident kan ikke være tom" }
        }
    }

    data class Applikasjon(
        val azpNavn: String,
        val clientId: String,
    ) : TokenPrincipal() {
        init {
            require(azpNavn.isNotBlank()) { "azpNavn kan ikke være tom" }
            require(clientId.isNotBlank()) { "clientId kan ikke være tom" }
        }
    }

    fun hentAktørId(): String =
        when (this) {
            is Bruker -> navIdent
            is Applikasjon -> azpNavn
        }
}
