package common.services.auth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
@ConditionalOnProperty("authentication.security.passthrough.enabled", havingValue = "true")
class PassThroughAuthService(
    val webClient: WebClient,
) : AuthService {
    val logger = LoggerFactory.getLogger(this::class.java)
    private var token: String = ""

    override fun getAuthToken(): String {
        return token
    }

    override fun setAuthToken(authToken: String) {
        token = authToken
    }
}
