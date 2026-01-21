package no.nav.gjenlevende.bs.sak.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

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

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return mapper
    }
}
