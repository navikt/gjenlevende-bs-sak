//package no.nav.gjenlevende.bs.sak.felles
//
//import org.slf4j.LoggerFactory
//import org.springframework.http.HttpRequest
//import org.springframework.http.client.ClientHttpRequestExecution
//import org.springframework.http.client.ClientHttpRequestInterceptor
//import org.springframework.http.client.ClientHttpResponse
//import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
//
//class BearerTokenInterceptor(
//    private val authorizedClientManager: OAuth2AuthorizedClientManager,
//    private val registrationId: String,
//    private val principalName: String = "application",
//) : ClientHttpRequestInterceptor {
//    private val log = LoggerFactory.getLogger(BearerTokenInterceptor::class.java)
//
//    override fun intercept(
//        request: HttpRequest,
//        body: ByteArray,
//        execution: ClientHttpRequestExecution,
//    ): ClientHttpResponse {
//        val accessToken =
//            authorizedClientManager
//                .authorize(
//                    OAuth2AuthorizeRequest
//                        .withClientRegistrationId(registrationId)
//                        .principal(principalName)
//                        .build(),
//                )?.accessToken
//                ?.tokenValue
//
//        if (accessToken.isNullOrBlank()) {
//            log.error("Kunne ikke hente token for registrationId={}", registrationId)
//            throw IllegalStateException("Kunne ikke hente token for $registrationId")
//        }
//
//        request.headers.setBearerAuth(accessToken)
//        return execution.execute(request, body)
//    }
//}
