package common.services

import common.model.TransferStat
import common.model.UserRegistration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.InetAddress

@Service
class UserInfo(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {

        var logger = LoggerFactory.getLogger(this::class.java)


    fun getUserRegistration(): UserRegistration? {
        var apiVersion: String = "/v2"
        val myRequest = webClient.get()
            .uri(apiVersion+"/User/CheckRegistration")

        val retrievedResource: Mono<UserRegistration> = myRequest
            .retrieve()
            .bodyToMono(UserRegistration::class.java)

        val result = retrievedResource.share().block()
        logger.info(result.toString())
        return result
    }


    fun selfRegister(): String? {
        logger.info(InetAddress.getLocalHost().getHostName())
        var apiVersion: String = "/v2"
        val myRequest = webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/User/Register")
                    .queryParam("displayName", "LeapService-${InetAddress.getLocalHost().getHostName()}")
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