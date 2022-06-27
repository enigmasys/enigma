package common.services

import common.model.UserRegistration
import common.services.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.InetAddress

@Service
class UserInfo(private val webClient: WebClient,
               val authService: AuthService
) {

    var logger = LoggerFactory.getLogger(this::class.java)


    fun getUserRegistration(): UserRegistration? {
        val token = authService.getAuthToken()
        var apiVersion: String = "/v2"
        val myRequest = webClient.get()
            .uri(apiVersion + "/User/CheckRegistration")
            .headers { it.setBearerAuth(token) }


        val retrievedResource: Mono<UserRegistration> = myRequest
            .retrieve()
            .bodyToMono(UserRegistration::class.java)

        val result = retrievedResource.share().block()
        logger.info(result.toString())
        return result
    }


    fun selfRegister(): String? {
        logger.info(InetAddress.getLocalHost().hostName)
        var apiVersion: String = "/v2"
        val myRequest = webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/User/Register")
                    .queryParam("displayName", "LeapService-${InetAddress.getLocalHost().hostName}")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)

        //

//
        val result = myRequest.share().block()
        logger.info(result.toString())
        return result
    }

}