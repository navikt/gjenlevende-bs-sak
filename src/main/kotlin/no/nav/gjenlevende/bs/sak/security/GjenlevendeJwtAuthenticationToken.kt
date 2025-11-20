package no.nav.gjenlevende.bs.sak.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken

class GjenlevendeJwtAuthenticationToken(
    private val jwt: Jwt,
    private val tokenPrincipal: TokenPrincipal,
    authorities: Collection<GrantedAuthority>,
) : AbstractOAuth2TokenAuthenticationToken<Jwt>(jwt, tokenPrincipal, jwt, authorities) {
    override fun getTokenAttributes(): Map<String, Any> = jwt.claims

    // Returnerer TokenPrincipal (Bruker eller Applikasjon).
    override fun getPrincipal(): TokenPrincipal = tokenPrincipal

    // Returnerer aktørens identifikator for logging.
    override fun getName(): String = tokenPrincipal.hentAktørId()
}
