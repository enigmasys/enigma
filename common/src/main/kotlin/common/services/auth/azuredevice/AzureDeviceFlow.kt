package common.services.auth.azuredevice

import com.microsoft.aad.msal4j.*
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@Component
//@ConditionalOnProperty("authentication.security.deviceflow.enabled", havingValue = "true")
class AzureDeviceFlow(
    @Value("\${spring.security.oauth2.client.registration.premonition.client-id}")
    private var client_id: String,
    @Value("\${spring.security.oauth2.client.provider.device_code.token-uri}")
    private var token_uri: String,
    @Value("\${spring.security.oauth2.client.registration.premonition.scope}")
    private var scope: Set<String>
) {


    //    private val authority: String? =
//        "https://login.microsoftonline.com/ba5a7f39-e3be-4ab3-b450-67fa80faecad/oauth2/v2.0/devicecode"
//    private val scope: Set<String>? = setOf("api://52094e65-d33d-4c6b-bd32-943bf4adec13/.default")
//    private val clientId: String? = "e98c2a73-e7b3-464c-a21a-51a5a2671094"

    //    @Throws(Exception::class)
    fun acquireTokenDeviceCode(): IAuthenticationResult? {
        val pca = PublicClientApplication.builder(client_id)
            .authority(token_uri)
            .setTokenCacheAccessAspect(PersistentTokenAspect().createPersistenceAspect())
            .build()

        val accountsInCache = pca.accounts.join()

        // Take first account in the cache. In a production application, you would filter
        // accountsInCache to get the right account for the user authenticating.
        var account: IAccount? = null
        if (accountsInCache.size > 0) {
            account = accountsInCache.iterator().next()
        }
        // Take first account in the cache. In a production application, you would filter
        // accountsInCache to get the right account for the user authenticating.

        var result: IAuthenticationResult? =
            try {
                if(account != null) {
                    val silentParameters = SilentParameters
                        .builder(scope, account)
                        .build()

                    // try to acquire token silently. This call will fail since the token cache
                    // does not have any data for the user you are trying to acquire a token for
                    pca.acquireTokenSilently(silentParameters).join()
                }
                else{
                    throw IllegalArgumentException("account** is null")
                }
            } catch (ex: java.lang.Exception) {
                if (ex.cause is MsalException ) {
                    try {
                        acquireTokenUsingDeviceFlow(pca)
                    }catch (ex: java.lang.Exception){
                        println("Exception: ${ex.message}")
                    }

                } else if(ex is IllegalArgumentException) {
                    // Handle other exceptions accordingly
                    try {
                        acquireTokenUsingDeviceFlow(pca)
                    }catch (ex: java.lang.Exception){
                        println("Exception: ${ex.message}")
                    }
                } else if (ex is MsalInteractionRequiredException){
                    try {
                        acquireTokenUsingDeviceFlow(pca)
                    }catch (ex: java.lang.Exception){
                        println("Exception: ${ex.message}")
                    }
                } else {
                    println("Other kind of exception...")
                    throw ex
                }
            } as IAuthenticationResult?
        return result
    }

    private fun acquireTokenUsingDeviceFlow(pca: PublicClientApplication): IAuthenticationResult? {
        val deviceCodeConsumer =
            Consumer { deviceCode: DeviceCode ->
                System.out.println(
                    deviceCode.message()
                )
            }
        val parameters = DeviceCodeFlowParameters
            .builder(scope, deviceCodeConsumer)
            .build()

        // Try to acquire a token via device code flow. If successful, you should see
        // the token and account information printed out to console, and the sample_cache.json
        // file should have been updated with the latest tokens.
        return pca.acquireToken(parameters).join()
    }


    private fun acquireTokenSilently(
        app: PublicClientApplication,
        account: IAccount
    ): CompletableFuture<IAuthenticationResult> {
        val authParams = SilentParameters
            .builder(scope, account)
            .build()
        return app.acquireTokenSilently(authParams)
    }

    fun getOidFromToken(token: String): String? {
        var jwtString = JWTParser.parse(token)
        println(jwtString.toString())
        var oid = (jwtString as SignedJWT).payload.toJSONObject().get("oid").toString()
        return oid
    }
}
