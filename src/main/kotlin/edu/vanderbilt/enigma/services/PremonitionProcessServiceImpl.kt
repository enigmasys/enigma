package edu.vanderbilt.enigma.services

import edu.vanderbilt.enigma.model.ProcessOwned
import edu.vanderbilt.enigma.model.ProcessState
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono

@Service
class PremonitionProcessServiceImpl(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {
//    @Autowired
//    lateinit var premWebClient: PremonitionClientConfig

    var apiVersion: String = "/v2"

    fun getListofProcesses(): ProcessOwned? {
        val myRequest = webClient.get()
            .uri(apiVersion+"/Process/ListOwnedProcesses")
//            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }

        val retrievedResource: Mono<ProcessOwned> = myRequest
            .retrieve()
            .bodyToMono(ProcessOwned::class.java)
        return retrievedResource.share().block()
    }


    fun getProcessState(processId: String) : ProcessState?{
        val myRequest = webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Process/GetProcessState")
                    .queryParam("processId", processId)
                    .build()
            }
//            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }

//        val response = myRequest.retrieve().bodyToMono(String::class.java).share().block()

        val retrievedResource: Mono<ProcessState> = myRequest
            .retrieve()
            .bodyToMono(ProcessState::class.java)
        val response = retrievedResource.share().block()
        println(response)
//        return retrievedResource.share().block()
        return response
    }
}