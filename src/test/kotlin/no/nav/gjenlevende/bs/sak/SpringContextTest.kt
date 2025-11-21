package no.nav.gjenlevende.bs.sak

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [ApplicationLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
    "integrasjonstest",
)
open class SpringContextTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @AfterEach
    fun reset() {
        resetWiremockServers()
    }

    private fun resetWiremockServers() {
        applicationContext.getBeansOfType(WireMockServer::class.java).values.forEach(WireMockServer::resetRequests)
    }
}
