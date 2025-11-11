package no.nav.gjenlevende.bs.sak

import org.springframework.boot.builder.SpringApplicationBuilder

class ApplicationLocal : ApplicationLocalSetup()

fun main(args: Array<String>) {
    SpringApplicationBuilder(ApplicationLocal::class.java)
        .initializers(DbContainerInitializer())
        .profiles(
            "integrasjonstest",
        ).run(*args)
}
