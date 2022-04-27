package common.services

import common.model.UserRegistration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class UserInfo(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {

        var logger = LoggerFactory.getLogger(this::class.java)


    fun getOID(): UserRegistration? {
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

}