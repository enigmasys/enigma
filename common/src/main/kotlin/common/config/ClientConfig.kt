package common.config

import org.springframework.web.reactive.function.client.WebClient

interface ClientConfig {
    fun apiWebClient(): WebClient
}