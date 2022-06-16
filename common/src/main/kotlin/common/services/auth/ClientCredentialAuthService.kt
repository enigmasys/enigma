package common.services

import common.model.auth.AzureToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.lang.Thread.sleep
import common.services.auth.AuthService as AuthService

@Component
@ConditionalOnProperty("authentication.security.client_credentials.enabled", havingValue = "true")
class ClientCredentialAuthService(
    val webClient: WebClient,
): AuthService {

    val logger = LoggerFactory.getLogger(this::class.java)


    @Value("\${spring.security.oauth2.client.provider.aad.token-uri}")
    private lateinit var token_uri: String
    @Value("\${spring.security.oauth2.client.registration.premonition.client-id}")
    private lateinit var client_id: String
    @Value("\${spring.security.oauth2.client.registration.premonition.client-secret}")
    private lateinit var  client_secret: String
    @Value("\${spring.security.oauth2.client.registration.premonition.scope}")
    private lateinit var  scope: String
    @Value("\${spring.security.oauth2.client.registration.premonition.authorization-grant-type}")
    private lateinit var  authorizationGrantType: String

    private var token:String = ""

    override fun getAuthToken() : String{
        fetchToken()
        return token
    }

    override fun setAuthToken(authToken: String) {
//        TODO("Not yet implemented")
    }

    fun fetchToken() {
        logger.info("Fetching the tokens")
        val dataMap = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", client_id)
            add("client_secret", client_secret)
            add("scope", scope)
        }
        token = webClient
            .post()
            .uri(token_uri)
            .body(BodyInserters.fromFormData(dataMap))
            .retrieve()
            .bodyToMono(AzureToken::class.java)
            .map { it.access_token }
            .onErrorReturn("").share().block().toString()
        sleep(100)
        logger.info("Token ${token}")
    }

}