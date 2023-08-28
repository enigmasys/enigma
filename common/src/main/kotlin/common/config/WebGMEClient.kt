package common.config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.ExchangeStrategies

@Configuration
class WebGMEClient : ClientConfig {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${cliclient.taxonomyServiceUrl:https://wellcomewebgme.centralus.cloudapp.azure.com}")
    private lateinit var WebGME_URL: String

    @Bean(name = ["WebGMEWebClient"])
//    @Qualifier("WebGMEWebClient")
    override fun apiWebClient(): WebClient {
        logger.debug("Calling the Generic WebClient...")
        return WebClient.builder()
            .baseUrl(WebGME_URL)
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().followRedirect(true)
                )
            )
            .filters { exchangeFilterFunctions ->
                exchangeFilterFunctions.add(LogFilter.logRequest())
                exchangeFilterFunctions.add(LogFilter.logResponse())
            }
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { clientCodecConfigurer ->
                        clientCodecConfigurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                    }
                    .build()
            )
            .build()
    }
}