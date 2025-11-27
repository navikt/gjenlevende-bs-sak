package no.nav.gjenlevende.bs.sak.pdl

data class PdlRequest(
    val query: String,
    val variables: Map<String, String>,
)

data class PdlResponse<T>(
    val data: T?,
    val errors: List<PdlError>? = null,
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>? = null,
    val path: List<String>? = null,
    val extensions: PdlErrorExtension? = null,
)

data class PdlErrorLocation(
    val line: Int,
    val column: Int,
)

data class PdlErrorExtension(
    val code: String? = null,
    val classification: String? = null,
)

data class HentPersonData(
    val hentPerson: HentPerson?,
)

data class HentPerson(
    val navn: List<Navn>,
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)

data class HentNavnRequest(
    val ident: String,
)
