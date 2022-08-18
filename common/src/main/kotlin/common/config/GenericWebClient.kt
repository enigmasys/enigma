package common.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GenericWebClient : ClientConfig {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${base_url:https://premonition.azurewebsites.net/}")
    private lateinit var baseUrl: String

    @Bean
    override fun apiWebClient(): WebClient {
        logger.debug("Calling the Generic WebClient...")
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}