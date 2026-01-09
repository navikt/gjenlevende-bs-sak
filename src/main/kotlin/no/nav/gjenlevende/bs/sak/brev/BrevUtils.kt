package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest

fun lagHtml(request: BrevRequest): String {
    fun esc(s: String) =
        s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    val tittel = esc(request.brevmal.tittel)
    val navn = esc(request.brevmal.informasjonOmBruker.navn)
    val fnr = esc(request.brevmal.informasjonOmBruker.fnr)
    val fritekst =
        request.fritekstbolker.joinToString(separator = "") { bolk ->
            val under = bolk.underoverskrift?.let { "<h2>${esc(it)}</h2>" } ?: ""
            "<section>\n\t${under}\n\t<p>${esc(bolk.innhold)}</p>\n</section>\n"
        }
    val avslutning =
        request.brevmal.fastTekstAvslutning.joinToString(separator = "") { bolk ->
            val under = bolk.underoverskrift?.let { "<h3>${esc(it)}</h3>" } ?: ""
            "<section class=\"avslutning\">\n\t${under}\n\t<p>${esc(bolk.innhold)}</p>\n</section>\n"
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
          </style>
        </head>
        <body>
          <header>
            <h1>$tittel</h1>
                <div class="meta"><strong>Navn:</strong> $navn
                <br/>
                <strong>Fødselsnummer:</strong> $fnr
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
