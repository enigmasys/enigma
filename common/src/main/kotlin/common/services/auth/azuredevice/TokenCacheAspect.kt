package common.services.auth.azuredevice

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect
import com.microsoft.aad.msal4j.ITokenCacheAccessContext
import org.springframework.util.ResourceUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey




// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
class TokenCacheAspect(fileName: String)
 : ITokenCacheAccessAspect {
    private var data: String
    private var cacheFileName:String

    private lateinit var fileEncrypterDecrypter: CryptoFile
    init {

        data = readDataFromFile(fileName)
        cacheFileName = fileName
//        val secretKey: SecretKey = KeyGenerator.getInstance("AES").generateKey()
//        val fileEncrypterDecrypter = CryptoFile(secretKey, "AES/CBC/PKCS5Padding")
    }

    override fun beforeCacheAccess(iTokenCacheAccessContext: ITokenCacheAccessContext) {
        iTokenCacheAccessContext.tokenCache().deserialize(data)
    }

    override fun afterCacheAccess(iTokenCacheAccessContext: ITokenCacheAccessContext) {
        data = iTokenCacheAccessContext.tokenCache().serialize()
        var file = File(cacheFileName)
        file.writeText(data)
//        fileEncrypterDecrypter.encrypt(data, "baz.enc");

        // you could implement logic here to write changes to file
    }

    companion object {
        private fun readDataFromFile(resource: String): String {
            return try {
                //Determine if sample running from IDE (resource URI starts with 'file') or from a .jar (resource URI starts with 'jar'),
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
//                    val uri = TokenCacheAspect::class.java.getResource(resource).toURI()
                    val uri = file.toURI()
                    val env: MutableMap<String, String?> = HashMap()
                    env["create"] = "true"
                    var isFileCreated = file.createNewFile()
//                    if(isFileCreated){
//                        println("$uri is created successfully.")
//                    } else{
//                        println("$uri already exists.")
//                    }

//                    val fs = FileSystems.newFileSystem(uri, env)
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