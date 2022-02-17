package edu.vanderbilt.enigma.services


import edu.vanderbilt.enigma.model.Observation.UploadObservationObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import org.springframework.web.reactive.function.client.WebClient


@Service
class ObservationUploadServiceImpl(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {
    @Autowired
    lateinit var premonitionProcessObj: PremonitionProcessServiceImpl
    var apiVersion:String = "/v2"

    fun appendObservation(  observationObject: UploadObservationObject) : String?{
        val processID = observationObject.processId
        // Get the processState for the number of observation counts
        val processStat = premonitionProcessObj.getProcessState(processID)
        // use the observation count as the index for the observationID.....
        val observationID = processStat?.numObservations
        observationObject.index = observationID!!
        apiVersion="/v2"
        val response = webClient.post()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Process/AppendObservation")
                    .queryParam("processId", processID)
                    .build()
            }
            .body(BodyInserters.fromPublisher(Mono.just(observationObject), UploadObservationObject::class.java))
            .retrieve()
            .bodyToMono(String::class.java)
        val data = response.share().block()
        return data
    }

    fun uploadObservationDataFile(){}
    fun uploadBatchObservationDataFiles(){}
    fun appendBatchObservations(){}
    fun updateObservation(){
    }
}







