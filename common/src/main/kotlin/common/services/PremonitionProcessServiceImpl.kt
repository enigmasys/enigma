package common.services

import common.model.ProcessState
import common.model.process.ProcessOwned
import common.services.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono

@Service
//@ComponentScan(*arrayOf("org.springframework.web.reactive.function.client.WebClient"))
//class PremonitionProcessServiceImpl(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {
class PremonitionProcessServiceImpl(
    val webClient: WebClient,
    val authService: AuthService
) {
//    @Autowired
//    lateinit var premWebClient: PremonitionClientConfig

    val logger = LoggerFactory.getLogger(this::class.java)
    var apiVersion: String = "/v2"

    fun getListofProcesses(): ProcessOwned? {
        val token = authService.getAuthToken()
        val myRequest = webClient.get()
//            .uri(apiVersion+"/Process/ListOwnedProcesses")
            .uri(apiVersion + "/Process/ListProcesses?permission=read")
            .headers { it.setBearerAuth(token) }
//            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }

        val retrievedResource: Mono<ProcessOwned> = myRequest
            .retrieve()
            .bodyToMono(ProcessOwned::class.java)
        return retrievedResource.share().block()
    }


    fun getProcessState(processId: String): ProcessState? {
        val token = authService.getAuthToken()
        val myRequest = webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Process/GetProcessState")
                    .queryParam("processId", processId)
                    .build()
            }
            .headers { it.setBearerAuth(token) }
//            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }

//        val response = myRequest.retrieve().bodyToMono(String::class.java).share().block()

        val retrievedResource: Mono<ProcessState> = myRequest
            .retrieve()
            .bodyToMono(ProcessState::class.java)
        val response = retrievedResource.share().block()
//        logger.info(response.toString())
//        return retrievedResource.share().block()
        return response
    }
}