package no.nav.gjenlevende.bs.sak.config

import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.env.Environment
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Optional
import javax.sql.DataSource

@Configuration
@EnableJdbcAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableJdbcRepositories("no.nav.familie", "no.nav.gjenlevende")
open class DatabaseConfig(
    private val objectMapper: ObjectMapper,
) : AbstractJdbcConfiguration() {
    @Bean
    open fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    @Bean
    open fun auditorAware(): AuditorAware<String> = AuditorAware { Optional.of(SikkerhetContext.hentSaksbehandlerEllerSystembruker()) }

    @Bean
    open fun dateTimeProvider(): DateTimeProvider = DateTimeProvider { Optional.of(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)) }

    override fun userConverters(): List<*> =
        listOf(
            PropertiesWrapperTilStringConverter(),
            StringTilPropertiesWrapperConverter(),
            BrevRequestTilJsonbConverter(objectMapper),
            JsonbTilBrevRequestConverter(objectMapper),
            YearMonthTilDateConverter(),
            DateTilYearMonthConverter(),
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
    class BrevRequestTilJsonbConverter(
        private val objectMapper: ObjectMapper,
    ) : Converter<BrevRequest, PGobject> {
        override fun convert(source: BrevRequest): PGobject =
            PGobject().apply {
                type = "jsonb"
                value = objectMapper.writeValueAsString(source)
            }
    }

    @ReadingConverter
    class JsonbTilBrevRequestConverter(
        private val objectMapper: ObjectMapper,
    ) : Converter<PGobject, BrevRequest> {
        override fun convert(source: PGobject): BrevRequest = objectMapper.readValue(source.value, BrevRequest::class.java)
    }

    @WritingConverter
    class YearMonthTilDateConverter : Converter<YearMonth, LocalDate> {
        override fun convert(source: YearMonth): LocalDate = source.atDay(1)
    }

    @ReadingConverter
    class DateTilYearMonthConverter : Converter<Date, YearMonth> {
        override fun convert(source: Date): YearMonth = YearMonth.from(source.toLocalDate())
    }
}
