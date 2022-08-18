package common.services


import common.model.observation.UploadObservationObject
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono


@Service
//class ObservationUploadServiceImpl(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {
class ObservationUploadServiceImpl(
    private val webClient: WebClient,
    val authService: AuthService
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var premonitionProcessObj: PremonitionProcessServiceImpl
    var apiVersion: String = "/v2"

    fun appendObservation(observationObject: UploadObservationObject): String? {
        val token = authService.getAuthToken()

        val processID = observationObject.processId
        // Get the processState for the number of observation counts
        val processStat = premonitionProcessObj.getProcessState(processID)
        // use the observation count as the index for the observationID.....
        val observationID = processStat?.numObservations
        observationObject.index = observationID!!
        apiVersion = "/v2"
        logger.debug(observationObject.toString())
        prettyJsonPrint(observationObject)
        val request = webClient.post()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Process/AppendObservation")
                    .queryParam("processId", processID)
                    .build()
            }
            .headers { it.setBearerAuth(token) }
            .contentType(MediaType.APPLICATION_JSON)
//            .body(Mono.just(observationObject),UploadObservationObject::class.java)
            .body(BodyInserters.fromPublisher(Mono.just(observationObject), UploadObservationObject::class.java))
//            .body(BodyInserters.fromPublisher(Mono.just(observationObject), UploadObservationObject::class.java))
//            .accept(MediaType.APPLICATION_JSON)
//            .body(BodyInserters.fromValue(observationObject))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .flatMap { clientResponse ->
                if (clientResponse.statusCode().is4xxClientError) {
                    clientResponse.body { clientHttpResponse, context -> clientHttpResponse.body }
                    return@flatMap clientResponse.bodyToMono(String::class.java)
                } else return@flatMap clientResponse.bodyToMono(String::class.java)
            }


//        val response = request
//            .retrieve()
//            .bodyToMono(String::class.java)
        val data = request.share().block()
//        val data = response.share().block()
        logger.debug(data)
        return data
    }

    fun uploadObservationDataFile() {}
    fun uploadBatchObservationDataFiles() {}
    fun appendBatchObservations() {}
    fun updateObservation() {
    }
}







