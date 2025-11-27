package no.nav.gjenlevende.bs.sak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
open class ApplicationLocal

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(
    value = ["scheduling.enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
open class SchedulingConfiguration

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
        .initializers(DbContainerInitializer())
        .profiles(
            "local",
        ).run(*args)
}
