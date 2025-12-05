package no.nav.gjenlevende.bs.sak.config

import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.client.RestClient

@SpringBootConfiguration
@ConfigurationPropertiesScan
@ComponentScan(
    "no.nav.familie.prosessering",
    "no.nav.gjenlevende.bs.sak",
)
open class ApplicationConfig {
    // TODO
    @Bean
    open fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String = "Dummy"

        override fun harTilgang(): Boolean = true
    }

    @Bean("utenAuthRestClient")
    fun utenAuthRestClient(
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestClient =
        RestClient
            .builder()
            .requestInterceptor { request, body, execution ->
                consumerIdClientInterceptor.intercept(request, body, execution)
            }.requestInterceptor(MdcValuesPropagatingClientInterceptor())
            .build()
}
