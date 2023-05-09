package edu.vanderbilt.enigma.WebServiceApp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(oAuth2AuthorizedClientManager: OAuth2AuthorizedClientManager?): WebClient {
        val function = ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager)
        return WebClient.builder()
            .apply(function.oauth2Configuration())
            .build()
    }
}