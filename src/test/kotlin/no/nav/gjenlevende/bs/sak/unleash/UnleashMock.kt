package no.nav.gjenlevende.bs.sak.unleash

import io.getunleash.Unleash
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile


@Configuration
@Profile("integrasjonstest")
open class UnleashMock {

    @Bean
    @Primary
    open fun unleash(): Unleash {
        val mockk = mockk<Unleash>()
        every { mockk.isEnabled(any()) } returns true
        return mockk
    }
}
