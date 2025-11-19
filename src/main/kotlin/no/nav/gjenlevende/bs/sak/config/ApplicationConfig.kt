package no.nav.gjenlevende.bs.sak.config

import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@SpringBootConfiguration
@EnableJdbcRepositories("no.nav.familie", "no.nav.gjenlevende")
@ComponentScan(
    "no.nav.familie.prosessering",
)
open class ApplicationConfig {

    //TODO
    @Bean
    open fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            "Dummy"

        override fun harTilgang(): Boolean = true
    }
}
