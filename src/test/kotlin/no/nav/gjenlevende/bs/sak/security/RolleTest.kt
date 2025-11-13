package no.nav.gjenlevende.bs.sak.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RolleTest {
    @Test
    fun `authority skal returnere ROLE_ prefiks med rollenavn`() {
        assertEquals("ROLE_SAKSBEHANDLER", Rolle.SAKSBEHANDLER.authority())
        assertEquals("ROLE_BESLUTTER", Rolle.BESLUTTER.authority())
        assertEquals("ROLE_VEILEDER", Rolle.VEILEDER.authority())
    }

    @Test
    fun `fraAzureGrupper skal mappe SAKSBEHANDLER Azure gruppe til SAKSBEHANDLER rolle`() {
        val azureGrupper = listOf("5357fbfa-de25-4d23-86a6-f67caf8ddd63")

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(1, roller.size)
        assertTrue(roller.contains(Rolle.SAKSBEHANDLER))
    }

    @Test
    fun `fraAzureGrupper skal mappe BESLUTTER Azure gruppe til BESLUTTER rolle`() {
        val azureGrupper = listOf("fda781b0-b82c-4049-919d-3b05623f05fb")

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(1, roller.size)
        assertTrue(roller.contains(Rolle.BESLUTTER))
    }

    @Test
    fun `fraAzureGrupper skal mappe VEILEDER Azure gruppe til VEILEDER rolle`() {
        val azureGrupper = listOf("0291bb72-71fa-4a35-9947-ea7b73f09ab8")

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(1, roller.size)
        assertTrue(roller.contains(Rolle.VEILEDER))
    }

    @Test
    fun `fraAzureGrupper skal mappe flere Azure grupper til flere roller`() {
        val azureGrupper =
            listOf(
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER
                "fda781b0-b82c-4049-919d-3b05623f05fb", // BESLUTTER
            )

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(2, roller.size)
        assertTrue(roller.contains(Rolle.SAKSBEHANDLER))
        assertTrue(roller.contains(Rolle.BESLUTTER))
    }

    @Test
    fun `fraAzureGrupper skal mappe alle Azure grupper til alle roller`() {
        val azureGrupper =
            listOf(
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER
                "fda781b0-b82c-4049-919d-3b05623f05fb", // BESLUTTER
                "0291bb72-71fa-4a35-9947-ea7b73f09ab8", // VEILEDER
            )

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(3, roller.size)
        assertTrue(roller.contains(Rolle.SAKSBEHANDLER))
        assertTrue(roller.contains(Rolle.BESLUTTER))
        assertTrue(roller.contains(Rolle.VEILEDER))
    }

    @Test
    fun `fraAzureGrupper skal returnere tomt sett for ukjent Azure gruppe`() {
        val azureGrupper = listOf("00000000-0000-0000-0000-000000000000")

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertTrue(roller.isEmpty())
    }

    @Test
    fun `fraAzureGrupper skal returnere tomt sett for tom liste`() {
        val azureGrupper = emptyList<String>()

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertTrue(roller.isEmpty())
    }

    @Test
    fun `fraAzureGrupper skal ignorere ukjente grupper og kun mappe gyldige`() {
        val azureGrupper =
            listOf(
                "00000000-0000-0000-0000-000000000000", // Ukjent
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER
                "11111111-1111-1111-1111-111111111111", // Ukjent
                "fda781b0-b82c-4049-919d-3b05623f05fb", // BESLUTTER
            )

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(2, roller.size)
        assertTrue(roller.contains(Rolle.SAKSBEHANDLER))
        assertTrue(roller.contains(Rolle.BESLUTTER))
    }

    @Test
    fun `fraAzureGrupper skal returnere unikt sett selv om samme gruppe oppgis flere ganger`() {
        val azureGrupper =
            listOf(
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER (duplikat)
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER (duplikat)
            )

        val roller = Rolle.fraAzureGrupper(azureGrupper)

        assertEquals(1, roller.size)
        assertTrue(roller.contains(Rolle.SAKSBEHANDLER))
    }

    @Test
    fun `skal ha korrekt beskrivelse for hver rolle`() {
        assertEquals("Kan saksbehandle i saksbehandler-løsningen", Rolle.SAKSBEHANDLER.beskrivelse)
        assertEquals("Kan fatte beslutninger i på saker i saksbehandling-løsningen", Rolle.BESLUTTER.beskrivelse)
        assertEquals("Kan veilede og gi støtte i saksbehandling-løsningen", Rolle.VEILEDER.beskrivelse)
    }
}
