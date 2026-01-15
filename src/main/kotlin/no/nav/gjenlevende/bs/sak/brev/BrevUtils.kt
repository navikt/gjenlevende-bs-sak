package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import java.util.Base64

private fun logoTilBase64(): String {
    val bytes =
        requireNotNull(
            object {}.javaClass.classLoader.getResourceAsStream("Nav-logo-red-228x63.png"),
        ) {
            "Fant ikke Nav-logo i resources"
        }.use { it.readBytes() }
    val base64 = Base64.getEncoder().encodeToString(bytes)
    return "data:image/png;base64,$base64"
}

fun lagHtml(request: BrevRequest): String {
    val tittel = request.brevmal.tittel
    val navn = request.brevmal.informasjonOmBruker.navn
    val personident = request.brevmal.informasjonOmBruker.fnr
    val logo = logoTilBase64()
    val fritekst =
        request.fritekstbolker.joinToString(separator = "") { bolk ->
            val under = bolk.underoverskrift?.let { "<h2>$it</h2>" } ?: ""
            "<section>\n\t${under}\n\t<p>${bolk.innhold}</p>\n</section>\n"
        }
    val avslutning =
        request.brevmal.fastTekstAvslutning.joinToString(separator = "") { bolk ->
            val under = bolk.underoverskrift?.let { "<h3>$it</h3>" } ?: ""
            "<section class=\"avslutning\">\n\t${under}\n\t<p>${bolk.innhold}</p>\n</section>\n"
        }

    return """
        <!DOCTYPE html>
        <html lang="no">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>$tittel</title>
          <style>
            body { font-family: Arial, Helvetica, sans-serif; font-size: 12pt; line-height: 1.4; }
            header { margin-bottom: 1rem; }
            h1 { font-size: 1.6rem; margin: 0 0 .5rem 0; }
            h2 { font-size: 1.2rem; margin: 1rem 0 .25rem 0; }
            h3 { font-size: 1rem; margin: .75rem 0 .25rem 0; }
            section { margin-bottom: .5rem; }
            .meta { color: #333; font-size: .9rem; }
            .header {
              position: relative;
              padding-top: 128px;
            }
            .logo {
              position: absolute;
              top: 64px;
              left: 64px;
              height: 16px;
              width: auto;
            }
          </style>
        </head>
        <body>
          <header class="header">
            <img class="logo" src="$logo" alt="Logo" height="32" />
            <h1>$tittel</h1>
                <div class="meta"><strong>Navn:</strong> $navn
                <br/>
                <strong>Fødselsnummer:</strong> $personident
            </div>
          </header>
          <main>
            $fritekst
            $avslutning
          </main>
        </body>
        </html>
        """.trimIndent()
}
