package no.nav.gjenlevende.bs.sak.config

import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.env.Environment
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import tools.jackson.databind.ObjectMapper
import javax.sql.DataSource
import kotlin.collections.contains

@Configuration
@EnableJdbcRepositories("no.nav.familie", "no.nav.gjenlevende")
open class DatabaseConfig(
    private val objectMapper: ObjectMapper,
) : AbstractJdbcConfiguration() {
    @Bean
    open fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    override fun userConverters(): List<*> =
        listOf(
            PropertiesWrapperTilStringConverter(),
            StringTilPropertiesWrapperConverter(),
            BrevRequestTilStringConverter(objectMapper),
            StringTilBrevRequestConverter(objectMapper),
        )

    @Bean
    open fun verifyIgnoreIfProd(
        @Value("\${spring.flyway.placeholders.ignoreIfProd}") ignoreIfProd: String,
        environment: Environment,
    ): FlywayConfigurationCustomizer {
        val isProd = environment.activeProfiles.contains("prod")
        val ignore = ignoreIfProd == "--"
        return FlywayConfigurationCustomizer {
            if (isProd && !ignore) {
                throw RuntimeException("Prod profile men har ikke riktig verdi for placeholder ignoreIfProd=$ignoreIfProd")
            }
            if (!isProd && ignore) {
                throw RuntimeException("Profile=${environment.activeProfiles} men har ignoreIfProd=--")
            }
        }
    }

    @WritingConverter
    class BrevRequestTilStringConverter(
        private val objectMapper: ObjectMapper,
    ) : Converter<BrevRequest, String> {
        override fun convert(brevRequest: BrevRequest): String = objectMapper.writeValueAsString(brevRequest)
    }

    @ReadingConverter
    class StringTilBrevRequestConverter(
        private val objectMapper: ObjectMapper,
    ) : Converter<String, BrevRequest> {
        override fun convert(source: String): BrevRequest = objectMapper.readValue(source, BrevRequest::class.java)
    }
}
