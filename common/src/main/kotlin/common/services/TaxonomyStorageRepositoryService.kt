package common.services

import common.model.ProcessState
import common.model.process.ProcessOwned
import common.services.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import org.springframework.stereotype.Service

@Service
class TaxonomyStorageRepositoryService (
    val webClient: WebClient, // this is the webclient to the Taxonomy server
    val authService: AuthService // This should hold both the cookie and the bearer token
) {
    val logger = LoggerFactory.getLogger(this::class.java)
    var apiVersion: String = "/v2"

    fun getRepoList(_token: String? = null): ProcessOwned? {
        // we will need cookie token and the bearer token
        var token: String?
        token = _token ?: authService.getAuthToken()

        // Call the webgme endpoint for the process list for the user.
        // This will return a list of processes that the user has access to.

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

}