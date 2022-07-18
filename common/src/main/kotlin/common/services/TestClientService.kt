package common.services


import common.services.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class TestClientService(
//    @Qualifier("premonitionApiWebClient") private val webClient: WebClient
    val webClient: WebClient,
    val authService: AuthService
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun getTestMessage(): String? {
        val token = authService.getAuthToken()
        var apiVersion: String = "/v2"
        val myRequest = webClient.get()
            .uri(apiVersion + "/Process/ListProcesses?permission=read")
            .headers { it.setBearerAuth(token) }
        val retrievedResource: Mono<String> = myRequest
            .retrieve()
            .bodyToMono(String::class.java)
        val result = retrievedResource.share().block()
        logger.info(result)
        return result
    }
}
