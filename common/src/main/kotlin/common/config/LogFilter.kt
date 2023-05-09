package common.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.util.function.Consumer

class LogFilter {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        fun logRequest(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                logger.debug("==================================================")
                logger.debug("Request: {} {}", clientRequest.method(), clientRequest.url())
                logHeaders(clientRequest)
                logger.debug("==================================================")
                Mono.just(clientRequest)
            }
        }

        fun logResponse(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { clientResponse ->

                logger.debug("Response: {} {}", clientResponse.statusCode(), clientResponse.bodyToMono(String::class.java))
                logStatus(clientResponse)
                logHeaders(clientResponse)
                return@ofResponseProcessor logBody(clientResponse)
            }
        }

        private fun logStatus(response: ClientResponse) {
            val status: HttpStatus = response.statusCode()
            logger.debug("Returned status code {} ({})", status.value(), status.reasonPhrase)
        }


        private fun logHeaders(response: ClientResponse) {
            response.headers().asHttpHeaders().forEach { name: String?, values: List<String?> ->
                values.forEach(
                    Consumer { value: String? -> logNameAndValuePair(name, value) })
            }
        }
        private fun logHeaders(request: ClientRequest) {
            request.headers().forEach { name: String?, values: List<String?> ->
                values.forEach(
                    Consumer { value: String? -> logNameAndValuePair(name, value) })
            }
        }
        private fun logNameAndValuePair(name: String?, value: String?) {
            logger.debug("{}={}", name, value)
        }

         private fun logBody(response: ClientResponse): Mono<ClientResponse> {
            return if (response.statusCode().is4xxClientError || response.statusCode().is5xxServerError) {
                response.bodyToMono(String::class.java)
                    .flatMap { body ->
                        logger.debug("Body is {}", body)
                        Mono.error(LEAPException(body, response.rawStatusCode()))
                    }
            } else {
                Mono.just(response)
            }
        }
    }
}

class LEAPException(message: String?, val statusCode: Int) : RuntimeException(message)