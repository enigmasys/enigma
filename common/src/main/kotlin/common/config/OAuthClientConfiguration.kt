//package common.config
//
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.ComponentScan
//import org.springframework.context.annotation.Configuration
//import org.springframework.http.HttpHeaders
//import org.springframework.security.config.web.server.ServerHttpSecurity
//import org.springframework.security.oauth2.client.*
//import org.springframework.security.oauth2.client.registration.ClientRegistration
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
//import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
//import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
//import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
//import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
//import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
//import org.springframework.security.oauth2.core.AuthorizationGrantType
//import org.springframework.security.web.server.SecurityWebFilterChain
//import org.springframework.web.reactive.function.client.ClientRequest
//import org.springframework.web.reactive.function.client.ClientResponse
//import org.springframework.web.reactive.function.client.ExchangeFilterFunction
//import org.springframework.web.reactive.function.client.WebClient
//import reactor.core.publisher.Mono
//
///**
// * @author Karanbir Singh on 07/18/2020
// */
//@Configuration
//@ConditionalOnProperty("authentication.security.client_credentials.enabled", havingValue = "true")
//class TestClientConfig : ClientConfig {
//
//    val logger = LoggerFactory.getLogger(this::class.java)
//    @Value("\${base_url:https://premonition.azurewebsites.net/}")
//    private lateinit var testClientBaseUrl: String
//    @Value("\${base_url:https://premonition.azurewebsites.net/}")
//    private lateinit var baseUrl: String
////
////    @Value("\${test.client.base.url}")
////    private val testClientBaseUrl: String? = null
//    private val testWebClientLogger = LoggerFactory.getLogger("TEST_WEB_CLIENT")
//
//    /**
//     * The authorizedClientManager for required by the webClient
//     */
////    @Bean
////    fun authorizedClientManager(
////        clientRegistrationRepository: ReactiveClientRegistrationRepository,
////        authorizedClientRepository: ServerOAuth2AuthorizedClientRepository
////    ): ReactiveOAuth2AuthorizedClientManager {
////        val authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
////            .clientCredentials()
////            .build()
////        val authorizedClientManager =
////            DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository)
////        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
////        return authorizedClientManager
////    }
//
//    @Bean
//    @ConditionalOnProperty("authentication.security.client_credentials.enabled", havingValue = "true")
//    fun clientRegistrations(
//        @Value("\${spring.security.oauth2.client.provider.aad.token-uri}") token_uri: String?,
//        @Value("\${spring.security.oauth2.client.registration.premonition.client-id}") client_id: String?,
//        @Value("\${spring.security.oauth2.client.registration.premonition.client-secret}") client_secret: String?,
//        @Value("\${spring.security.oauth2.client.registration.premonition.scope}") scope: String?,
//        @Value("\${spring.security.oauth2.client.registration.premonition.authorization-grant-type}") authorizationGrantType: String?
//    ): ReactiveClientRegistrationRepository {
//        val registration = ClientRegistration
//            .withRegistrationId("premonition")
//            .tokenUri(token_uri)
//            .clientId(client_id)
//            .clientSecret(client_secret)
//            .scope(scope)
//            .authorizationGrantType(AuthorizationGrantType(authorizationGrantType))
//            .build()
//        return InMemoryReactiveClientRegistrationRepository(registration)
//    }
//
//
////
////    /**
////     * The Oauth2 based WebClient bean for the web service
////     */
////    @Bean("testWebClient")
////    fun webClient(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager?): WebClient {
////        val registrationId = "premonition"
////        val oauth = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
////        // for telling which registration to use for the webclient
////        oauth.setDefaultClientRegistrationId(registrationId)
////        return WebClient.builder() // base path of the client, this way we need to set the complete url again
////            .baseUrl(testClientBaseUrl!!)
////            .filter(oauth)
//////            .filter(logRequest())
//////            .filter(logResponse())
////            .build()
////    }
//
////    /*
////     * Log request details for the downstream web service calls
////     */
////    private fun logRequest(): ExchangeFilterFunction {
////        return ExchangeFilterFunction.ofRequestProcessor { c: ClientRequest ->
////            testWebClientLogger.info("Request: {} {}", c.method(), c.url())
////            c.headers()
////                .forEach { n: String, v: List<String?>? ->
////                    if (!n.equals(HttpHeaders.AUTHORIZATION, ignoreCase = true)) {
////                        testWebClientLogger.info("request header {}={}", n, v)
////                    } else {
////                        // as the AUTHORIZATION header is something security bounded
////                        // will show up when the debug level logging is enabled
////                        // for example using property - logging.level.root=DEBUG
////                        testWebClientLogger.debug("request header {}={}", n, v)
////                    }
////                }
////            Mono.just(c)
////        }
////    }
//
////    /*
////     * Log response details for the downstream web service calls
////     */
////    private fun logResponse(): ExchangeFilterFunction {
////        return ExchangeFilterFunction.ofResponseProcessor { c: ClientResponse ->
////            testWebClientLogger.info("Response: {}", c.statusCode())
////            Mono.just(c)
////        }
////    }
//
//    @Bean
//    @ConditionalOnProperty("authentication.security.client_credentials.enabled", havingValue = "true")
//    fun premonitionApiWebClient(clientRegistrations: ReactiveClientRegistrationRepository?): WebClient {
//        logger.info("Activating the premonitionAPI")
//        val clientService = InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations)
//        val authorizedClientManager =
//            AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService)
//        val oauth = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
//        oauth.setDefaultClientRegistrationId("premonition")
//        return WebClient.builder()
//            .baseUrl(baseUrl)
//            .filter(oauth)
//            .build()
//    }
//
//    override fun ApiWebClient(): WebClient {
//        TODO("Not yet implemented")
//    }
//
////     This is needed to let the rest controller accept the client credential workflow authorization...
////    @Bean
////    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
////        return http.oauth2Client().and().build()
////    }
//}