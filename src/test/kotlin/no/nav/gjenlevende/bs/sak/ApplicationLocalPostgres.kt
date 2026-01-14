package no.nav.gjenlevende.bs.sak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.Properties

// @EnableMockOAuth2Server
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(
    value = ["scheduling.enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class ApplicationLocalPostgres

fun main(args: Array<String>) {
    val properties = Properties()
    properties["DATASOURCE_URL"] = "jdbc:postgresql://localhost:5432/gjenlevende-bs-sak"
    properties["DATASOURCE_USERNAME"] = "postgres"
    properties["DATASOURCE_PASSWORD"] = "test"
    properties["DATASOURCE_DRIVER"] = "org.postgresql.Driver"

    SpringApplicationBuilder(ApplicationLocalPostgres::class.java)
        .profiles(
            "local",
        ).properties(properties)
        .run(*args)
}
