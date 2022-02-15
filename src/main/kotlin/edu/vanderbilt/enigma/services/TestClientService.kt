package edu.vanderbilt.enigma.services


import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class TestClientService(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {

    fun getTestMessage(): String? {
        var apiVersion: String = "/v2"
        val myRequest = webClient.get()
            .uri(apiVersion+"/Process/ListOwnedProcesses")
        val retrievedResource: Mono<String> = myRequest
            .retrieve()
            .bodyToMono(String::class.java)
        val result = retrievedResource.share().block()
        println(result)
        return result

    }
}
