package no.nav.gjenlevende.bs.sak

import org.springframework.boot.builder.SpringApplicationBuilder

open class ApplicationLocal : ApplicationLocalSetup()

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
        .initializers(DbContainerInitializer())
        .profiles(
            "local",
        ).run(*args)
}
