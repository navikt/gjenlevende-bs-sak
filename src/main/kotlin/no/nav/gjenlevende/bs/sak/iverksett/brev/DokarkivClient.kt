// package no.nav.gjenlevende.bs.sak.iverksett.brev
//
// import no.nav.gjenlevende.bs.sak.texas.TexasClient
// import org.slf4j.LoggerFactory
// import org.springframework.beans.factory.annotation.Value
// import org.springframework.http.HttpHeaders
// import org.springframework.http.MediaType
// import org.springframework.stereotype.Component
// import org.springframework.web.client.RestClient
//
// @Component
// class DokarkivClient(
//    private val texasClient: TexasClient,
//    @Value("\${DOKARKIV_URL}")
//    private val dokarkivUrl: String,
//    @Value("\${DOKARKIV_SCOPE}")
//    private val dokarkivScope: String,
// ) {
//    private val logger = LoggerFactory.getLogger(DokarkivClient::class.java)
//    val dokarkivRestClient =
//        RestClient
//            .builder()
//            .baseUrl(dokarkivUrl)
//            .defaultHeader("Content-Type", "application/json")
//            .build()
//
//    fun opprettJournalpost(
//        request: OpprettJournalpostRequest,
//        forsoekFerdigstill: Boolean = false,
//    ): OpprettJournalpostResponse {
//        logger.info("Oppretter journalpost i dokarkiv")
//        try {
//            val response =
//                dokarkivRestClient
//                    .post()
//                    .uri("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=$forsoekFerdigstill")
//                    .headers { it.addAll(lagDokarkivHeaders()) }
//                    .body(request)
//                    .retrieve()
//                    .body(OpprettJournalpostResponse::class.java)
//
//            return response ?: throw DokarkivException("Ingen respons fra dokarkiv ved opprettelse av journalpost")
//        } catch (e: Exception) {
//            when (e) {
//                is DokarkivException -> {
//                    throw e
//                }
//
//                else -> {
//                    logger.error("Feil ved kall til dokarkiv", e)
//                    throw DokarkivException("Teknisk feil ved opprettelse av journalpost i dokarkiv", e)
//                }
//            }
//        }
//    }
//
//    fun ferdigstillJournalpost(
//        journalpostId: String,
//        journalfoerendeEnhet: String,
//    ) {
//        logger.info("Ferdigstiller journalpost $journalpostId i dokarkiv")
//        try {
//            dokarkivRestClient
//                .patch()
//                .uri("/rest/journalpostapi/v1/journalpost/$journalpostId/ferdigstill")
//                .headers { it.addAll(lagDokarkivHeaders()) }
//                .body(FerdigstillJournalpostRequest(journalfoerendeEnhet))
//                .retrieve()
//                .toBodilessEntity()
//        } catch (e: Exception) {
//            logger.error("Feil ved ferdigstilling av journalpost $journalpostId", e)
//            throw DokarkivException("Teknisk feil ved ferdigstilling av journalpost i dokarkiv", e)
//        }
//    }
//
//    private fun lagDokarkivHeaders(): HttpHeaders =
//        HttpHeaders().apply {
//            contentType = MediaType.APPLICATION_JSON
//            set("Authorization", "Bearer ${texasClient.hentOboToken(dokarkivScope)}")
//        }
// }
//
// class DokarkivException(
//    message: String,
//    cause: Throwable? = null,
// ) : RuntimeException(message, cause)
