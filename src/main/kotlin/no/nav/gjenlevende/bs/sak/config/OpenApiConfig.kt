package no.nav.gjenlevende.bs.sak.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfig {
    @Bean
    open fun apiConfig(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Gjenlevende BS Sak")
                    .description("Swagger for gjenlevende-bs-sak som holder forvaltning og test kall.")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Team Etterlatte")
                            .url("https://github.com/navikt/gjenlevende-bs-sak"),
                    ),
            ).servers(
                listOf(
                    Server().url("/").description("Samme server som Swagger UI"),
                    Server().url("https://gjenlevende-bs-sak.intern.dev.nav.no").description("Kjører i pre-prod miljø"),
                ),
            )
}
