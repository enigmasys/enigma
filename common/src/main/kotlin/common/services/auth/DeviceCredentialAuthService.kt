package common.services.auth

import com.microsoft.aad.msal4j.IAuthenticationResult
import common.services.auth.azuredevice.AzureDeviceFlow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
@ConditionalOnProperty("authentication.security.deviceflow.enabled", havingValue = "true")
@ComponentScan(basePackages = ["common.services.auth.azuredevice"])
class DeviceCredentialAuthService(
    val webClient: WebClient,
) : AuthService {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${spring.security.oauth2.client.provider.aad.token-uri}")
    private lateinit var tokenUri: String

    @Value("\${spring.security.oauth2.client.registration.premonition.client-id}")
    private lateinit var clientId: String

    @Value("\${spring.security.oauth2.client.registration.premonition.scope}")
    private lateinit var scope: Set<String>

    private var token: String = ""

    override fun getAuthToken(): String {
        fetchToken()
        return token
    }

    override fun setAuthToken(authToken: String) {
//        TODO("Not yet implemented")
    }

    fun fetchToken() {
        logger.info("Fetching Device Code Token,,,")
        var result: IAuthenticationResult? = null
        try {
            result = AzureDeviceFlow(clientId, tokenUri, scope).acquireTokenDeviceCode()
        } catch (ex: Exception) {
            println("Encounter Exception: $ex")
        }

//        println("Access token: " + result?.accessToken())
        token = result?.accessToken().toString()
//        println("Id token: " + result?.idToken())
//        println("Account username: " + result?.account()?.username())
//        println("Observer ID:"+ (result?.account()?.homeAccountId()?.split(".")?.get(0)))
//        logger.info("Token ${token}")
    }

}