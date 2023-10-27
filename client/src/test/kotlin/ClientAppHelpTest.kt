import kotlin.test.Test
import kotlin.test.assertEquals

import edu.vanderbilt.enigma.client.ClientApp
class ClientAppHelpTest {


    @Test
    fun `Test Client: CLI help using -h`() {

        val capture = CaptureStdOutAndStderr()
        var client = ClientApp()
        client.run(["-h"])

        var expected() =
        """
            Usage: <main class> [-hV] [-t=<token>] [COMMAND]
            Command for accessing the UDCP
              -h, --help            Show this help message and exit.
              -t, --token=<token>   Auth Token to pass when using Auth Passthrough Mode!
              -V, --version         Print version information and exit.
            Commands:
              process, proc, repository, repo
              download, pull
              upload, push
              userinfo, user
        """.trimIndent()

        assertEquals(capture.output, expected)

    }

    @Test
    fun `Test Client: CLI help using --help`() {
        val capture = CaptureStdOutAndStderr()
        var client = ClientApp()
        client.run(["--help"])

        var expected() =
        """
            Usage: <main class> [-hV] [-t=<token>] [COMMAND]
            Command for accessing the UDCP
              -h, --help            Show this help message and exit.
              -t, --token=<token>   Auth Token to pass when using Auth Passthrough Mode!
              -V, --version         Print version information and exit.
            Commands:
              process, proc, repository, repo
              download, pull
              upload, push
              userinfo, user
        """.trimIndent()

        assertEquals(capture.output, expected)
    }
}