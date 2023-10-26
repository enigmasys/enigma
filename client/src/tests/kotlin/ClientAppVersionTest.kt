import kotlin.test.Test
import kotlin.test.assertEquals

import edu.vanderbilt.enigma.client.ClientApp

class ClientAppVersionTest {

    @Test

    fun `Test Client: CLI version using -V`() {
        val capture = CaptureStdOutAndStderr()
        var client = ClientApp()

        client.run(["-V"])

        assertTrue(capture.contains("Current CLI Version is:"))
    }

    fun `Test Client: CLI version using --version`() {
        val capture = CaptureStdOutAndStderr()
        var client = ClientApp()

        client.run(["--version"])

        assertTrue(capture.contains("Current CLI Version is:"))
    }

}