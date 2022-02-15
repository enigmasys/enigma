package edu.vanderbilt.enigma.services

import com.azure.storage.blob.BlobClientBuilder

import edu.vanderbilt.enigma.model.Directory
import edu.vanderbilt.enigma.model.EgressData
import edu.vanderbilt.enigma.model.TransferStat
import edu.vanderbilt.enigma.model.UploadObservationObject
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.io.OutputStream

@Service
class ObservationServiceImpl(@Qualifier("premonitionApiWebClient") private val webClient: WebClient) {
    var apiVersion:String = "/v2"

    fun createTemporaryDirectory(
        processID: String,
        expiresInMins: Int = 360,
        isUpload: Boolean = false
    ): Directory? {
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion+"/Files/CreateDirectory")
                    .queryParam("processId", processID)
                    .queryParam("expiresInMins", expiresInMins)
                    .queryParam("isUpload",isUpload)
                    .build()
            }
            .retrieve()
            .bodyToMono(Directory::class.java)
        val reply = response.share().block()
        return reply
    }

    fun getObservationFiles(
        processID: String,
        directoryID: String,
        startObsIndex: String,
        endObsIndex: String
    ): String {
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion+"/Files/GetObservationFiles")
                    .queryParam("processId", processID)
                    .queryParam("directoryId", directoryID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("filePattern", "**/*.*")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
        val transferId = response.share().block().toString()
        return transferId
    }

    fun downloadFile(url: String, outputStream: OutputStream?) {
        val blobClient = BlobClientBuilder()
            .endpoint(url)
            .buildClient()
        val dataSize = blobClient.properties.blobSize.toInt()
        return blobClient.downloadStream(outputStream);
    }

    fun downloadResourceFile(url: String){

    }

    // Get Observation Files....
    fun getObservationsV3(
        processID: String,
        startObsIndex: String,
        endObsIndex: String,
        expiresInMin: String = "60",
//        authorizedClient: OAuth2AuthorizedClient?
    ): EgressData? {
        apiVersion="/v3"
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Files/GetObservations")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("expiresInMins",expiresInMin)
                    .build()
            }
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }
            .retrieve()
            .bodyToMono(EgressData::class.java)
        val data = response.share().block()
        val sasUrlList:ArrayList<String> =ArrayList<String>()
        data?.files?.forEach {it -> sasUrlList.add(it.sasUrl)}
        return data

//        return sasUrlList

    }

    fun getObservation(
        processID: String,
        startObsIndex: String,
        version: String,
//        authorizedClient: OAuth2AuthorizedClient?
    ): String? {
        apiVersion="/v2"
        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Process/GetObservation")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("version", version)
                    .build()
            }
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }
            .retrieve()
            .bodyToMono(String::class.java)
        val data = response.share().block()
//        val sasUrlList:ArrayList<String> =ArrayList<String>()
//        data?.files?.forEach {it -> sasUrlList.add(it.sasUrl)}
        return data
    }

    fun getTransferStat(
        processID: String,
        directoryID: String,
        transferId: String,
    ): TransferStat? {
        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("/Files/GetTransferState")
                    .queryParam("processId", processID)
                    .queryParam("directoryId", directoryID)
                    .queryParam("transferId", transferId)
                    .build()
            }
            .retrieve()
            .bodyToMono(TransferStat::class.java)
        val transString = response.share().block()
        return transString
    }

    fun getObservationFilesV3(processID: String, startObsIndex: String, endObsIndex: String, expiresInMins: String): String? {
        apiVersion="/v3"
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion+"/Files/GetObservationFiles")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("expiresInMins",expiresInMins)
                    .queryParam("filePattern", "**/*.*")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
        val transferId = response.share().block().toString()
        return transferId
    }


    fun listDirectories(processID: String, isUpload: String): String?{
        apiVersion = "/v2"

        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion+"/Files/ListDirectories")
                    .queryParam("processId", processID)
                    .queryParam("isUpload",isUpload)
                    .build()
            }
//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }
            .retrieve()
            .bodyToMono(String::class.java)
        val transferId = response.share().block().toString()
        return transferId
    }

    fun putObservationFiles(processID: String,
                            directoryId: String,
                            startObsIndex: String,
                            endObsIndex: String,
                            version: String,
                            dataFiles: List<String>
//                            authorizedClient: OAuth2AuthorizedClient?
    ): String {
        apiVersion="/v2"
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion+"/Files/PutObservationFiles")
                    .queryParam("processId", processID)
                    .queryParam("directoryId", directoryId)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("version", version)
                    .build()
            }
            .bodyValue(dataFiles)
            .retrieve()
            .bodyToMono(String::class.java)
        val transferId = response.share().block().toString()
        return transferId
    }

    fun appendVersions(observationObject: UploadObservationObject) : String? {
        apiVersion = "/v2"
        val response = webClient
            .post()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion+"/Process/AppendVersion")
                    .build()
            }
            .body(BodyInserters.fromPublisher(Mono.just(observationObject), UploadObservationObject::class.java))
            .retrieve()
            .bodyToMono(String::class.java)
        val result = response.share().block().toString()
        return result
    }

}