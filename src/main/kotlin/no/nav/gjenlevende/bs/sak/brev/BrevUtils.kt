package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.TekstbolkDto
import java.util.Base64

fun logoTilBase64(): String {
    val bytes =
        requireNotNull(
            object {}.javaClass.classLoader.getResourceAsStream("Nav-logo-red-228x63.png"),
        ) {
            "Fant ikke Nav-logo i resources"
        }.use { it.readBytes() }
    val base64 = Base64.getEncoder().encodeToString(bytes)
    return "data:image/png;base64,$base64"
}

fun lagHtmlTekstbolker(tekstbolker: List<TekstbolkDto>): String =
    tekstbolker.joinToString(separator = "") { bolk ->
        val underoverskrift = bolk.underoverskrift?.let { "<h2>$it</h2>" } ?: ""
        "<section>\n${underoverskrift}\n<p>${bolk.innhold}</p>\n</section>\n"
    }
