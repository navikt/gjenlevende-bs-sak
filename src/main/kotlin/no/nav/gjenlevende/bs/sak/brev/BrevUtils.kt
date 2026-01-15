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
            <style type="text/css">
                body { font-family: Arial, Helvetica, sans-serif; font-size: 11pt; line-height: 12pt; }
                header { margin-bottom: 12pt; }
                h1 { font-size: 16pt; line-height: 20pt; font-weight: 700; margin-bottom: 26pt; }
                h2 { font-size: 13pt; line-height: 16pt; font-weight: 700; margin-bottom: 6pt;}
                h3 { font-size: 12pt; line-height: 16pt; font-weight: 700; margin-bottom: 6pt;}
                section { margin-bottom: 26pt; }
                .infoBruker { color: #333; font-size: 11pt; }
                .header {
                    position: relative;
                    padding-top: 128pt;
                }
                .logo {
                    position: absolute;
                    top: 64pt;
                    left: 64pt;
                    height: 16pt;
                    width: auto;
                }
            </style>
        </head>
        <body>
          <header class="header">
            <img class="logo" src="$logo" alt="Logo" height="16" />
            <div class="infoBruker">
                <strong>Navn:</strong> $navn
                <br/>
                <strong>Fødselsnummer:</strong> $personident
            </div>
          </header>
          <main>
            <h1>$tittel</h1>
            $fritekst
            $avslutning
          </main>
        </body>
        </html>
        """.trimIndent()
}
