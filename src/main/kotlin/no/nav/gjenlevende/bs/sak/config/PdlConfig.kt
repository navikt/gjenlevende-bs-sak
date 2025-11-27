package no.nav.gjenlevende.bs.sak.config

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.io.readText
import kotlin.jvm.java
import kotlin.text.replace

@Configuration
open class PdlConfig(
    @Value("\${PDL_URL}") pdlUrl: URI,
) {
    val pdlUri: URI =
        UriComponentsBuilder
            .fromUri(pdlUrl)
            .pathSegment(PATH_GRAPHQL)
            .build()
            .toUri()

    companion object {
        const val PATH_GRAPHQL = "graphql"

        val hentNavnQuery = graphqlQuery("/pdl/hent_navn.graphql")

        private fun graphqlQuery(path: String) =
            PdlConfig::class.java
                .getResource(path)
                .readText()
                .graphqlCompatible()

        private fun String.graphqlCompatible(): String = StringUtils.normalizeSpace(this.replace("\n", ""))
    }
}
