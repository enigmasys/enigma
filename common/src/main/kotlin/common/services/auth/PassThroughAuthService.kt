package common.services.auth


import common.model.auth.AzureToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import common.services.auth.AuthService as AuthService

@Component
@ConditionalOnProperty("authentication.security.passthrough.enabled", havingValue = "true")
class PassThroughAuthService (
    val webClient: WebClient
): AuthService {
    val logger = LoggerFactory.getLogger(this::class.java)
    private var token:String = ""


    override fun getAuthToken() : String{
        return token
    }


    override fun setAuthToken(authToken: String) {
        token = authToken
    }

}