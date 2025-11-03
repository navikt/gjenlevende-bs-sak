package no.nav.gjenlevende.bs.sak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class GjenlevendeBsSakApplication

fun main(args: Array<String>) {
    runApplication<GjenlevendeBsSakApplication>(*args)
}
