package common.services.auth.azuredevice

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect
import com.microsoft.aad.msal4j.ITokenCacheAccessContext
import org.springframework.util.ResourceUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class TokenCacheAspect(fileName: String)
 : ITokenCacheAccessAspect {
    private var data: String
    private var cacheFileName:String
    init {
        // surround with try catch
        try {
            data = CryptHelper.decrypt("Bar12345Bar12346", readDataFromFile(fileName))!!
        }catch (e: Exception) {
            println("Error decrypting the data from the file: $fileName")
            data = ""
        }
        cacheFileName = fileName
    }

    override fun beforeCacheAccess(iTokenCacheAccessContext: ITokenCacheAccessContext) {
        iTokenCacheAccessContext.tokenCache().deserialize(data)
    }

    override fun afterCacheAccess(iTokenCacheAccessContext: ITokenCacheAccessContext) {
        data = iTokenCacheAccessContext.tokenCache().serialize()
        var file = File(cacheFileName)

        val encryptData = CryptHelper.encrypt("Bar12345Bar12346", data)
        file.writeText(encryptData!!)
    }

    companion object {
        private fun readDataFromFile(resource: String): String {
            return try {
                //Determine if sample running from IDE (resource URI starts with 'file')
                // or from a .jar (resource URI starts with 'jar'),
                //  so that sample_cache.json is read properly
                val file: File = ResourceUtils.getFile(resource)
                if(file.exists()){
                    return String(
                        Files.readAllBytes(
                            Paths.get(file.toURI())
                        )
                    )
                }
                else {
                    val uri = file.toURI()
                    val myFolderPath = Paths.get(uri)
                    return String(Files.readAllBytes(myFolderPath))
                }
            } catch (ex: Exception) {
                println("Error reading data from file: " + ex.message)
                throw RuntimeException(ex)
            }
        }
    }
}
