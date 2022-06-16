package common.services.auth.azuredevice

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect
import com.microsoft.aad.msal4jextensions.PersistenceSettings
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths



@ConditionalOnProperty("authentication.security.deviceflow.enabled", havingValue = "true")
class PersistentTokenAspect{

    @Throws(IOException::class)
    private fun createPersistenceSettings(): PersistenceSettings? {
//        val path: Path = Paths.get(System.getProperty("user.home"), "MSAL", "testCache")
        val path: Path = Paths.get("").toAbsolutePath()
        println("File path is $path")
        return PersistenceSettings.builder("testCacheFile", path)
            .setMacKeychain("MsalTestService", "MsalTestAccount")
            .setLinuxKeyring(
                null,
                "MsalTestSchema",
                "MsalTestSecretLabel",
                "MsalTestAttribute1Key",
                "MsalTestAttribute1Value",
                "MsalTestAttribute2Key",
                "MsalTestAttribute2Value"
            )
            .setLockRetry(1000, 50)
            .build()
    }

    @Throws(IOException::class)
    fun createPersistenceAspect(): ITokenCacheAccessAspect? {
        return PersistenceTokenCacheAccessAspect(createPersistenceSettings())
    }
}