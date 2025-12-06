package no.nav.gjenlevende.bs.sak.opplysninger

import org.hibernate.validator.constraints.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class FamilieIntegrasjonerConfig(
    @Value("\${FAMILIE_INTEGRASJONER_URL}") private val url: String,
    @Value("\${FAMILIE_INTEGRASJONER_SCOPE}") private val scope: String,
)
