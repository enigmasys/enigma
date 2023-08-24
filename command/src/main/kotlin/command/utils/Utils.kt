package command.utils
import org.springframework.web.reactive.function.client.WebClient

@Suppress("unused")
class Release {
    var html_url: String? = ""
    var tag_name: String? = ""
    var name: String? = ""
    var published_at: String? = ""
    var prerelease: Boolean = false
}

object Utils {

    private fun checkGithubforUpdates(CLI_RELEASE_URL: String) : Release?{
        println("Checking for updates...")
        val webClient = WebClient.create()
        val response = webClient.get()
            .uri(CLI_RELEASE_URL)
            .retrieve()
            .bodyToMono(Release::class.java)
            .block()
        return response
    }

    private fun getCLIReleaseURL(): String {
        val props = this::class.java.classLoader.getResourceAsStream("version.properties").use {
            java.util.Properties().apply { load(it) }
        }
        return props["releaseURL"] as String
    }

    private fun getCLIVersion(): String {
        val props = this::class.java.classLoader.getResourceAsStream("version.properties").use {
            java.util.Properties().apply { load(it) }
        }
        return props["version"] as String
    }

    private fun getLatestCLIRelease(): Release? {
        val CLI_RELEASE_URL = getCLIReleaseURL()
        return checkGithubforUpdates(CLI_RELEASE_URL)
    }

    private fun checkIfNewCLIReleaseAvailable(currentRelease: String, latestRelease: String): Boolean {
        return currentRelease!=latestRelease
    }

    fun getCurrentAndReleaseVersion(): Array<String> {
        val currentRelease = getCLIVersion()
        println("Current CLI Version is: $currentRelease")
        try {
            val latestRelease = getLatestCLIRelease()

            latestRelease?.let{
                if (checkIfNewCLIReleaseAvailable(currentRelease, latestRelease.tag_name!!)) {

                    println("===========================================")
                    println("New CLI release available: ${latestRelease.tag_name}")
                    println("Download it from: ${latestRelease.html_url}")
                    println("===========================================")

                } else {
                    println("===========================================")
                    println("Latest Release: ${latestRelease.tag_name}  was " +
                            "published at: ${latestRelease.published_at}")
                    println("Latest Release can be found at ${latestRelease.html_url}" )
                    println("This CLI version is up to date.")
                    println("===========================================")

                }
            }

            if (latestRelease != null) {
                return arrayOf("Current CLI Version: $currentRelease",
                    "Latest Release Version: ${latestRelease.tag_name!!}")
            }
        }catch (e: Exception) {
            println("Error checking for updates: ${e.message}")
        }
        return arrayOf("CLI Version: $currentRelease")
    }
}