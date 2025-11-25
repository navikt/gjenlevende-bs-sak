package no.nav.gjenlevende.bs.sak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class WebClientConfig {
    @Bean
    open fun infotrygdWebClient(
        @Value("\${gjenlevende-bs-infotrygd.url}")
        infotrygdUrl: String,
    ): WebClient =
        WebClient
            .builder()
            .baseUrl(infotrygdUrl)
            .defaultHeader("Content-Type", "application/json")
            .build()

    @Bean(name = ["azureClientCredential"])
    open fun azureClientCredentialRestTemplate(): RestOperations = RestTemplate()
}
