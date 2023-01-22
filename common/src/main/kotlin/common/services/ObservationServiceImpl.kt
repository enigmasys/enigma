package common.services

import com.azure.storage.blob.BlobClientBuilder
import common.model.Directory
import common.model.TransferStat
import common.model.observation.EgressResult
import common.model.observation.PeekObservationResult
import common.model.observation.UploadObservationObject
import common.services.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.io.OutputStream
import java.lang.Integer.max
import java.lang.Integer.min
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

@Service
class ObservationServiceImpl(
    private val webClient: WebClient,
    val authService: AuthService
) {
    var logger = LoggerFactory.getLogger(this::class.java)
    var apiVersion: String = "/v2"

    fun createTemporaryDirectory(
        processID: String,
        expiresInMins: Int = 360,
        isUpload: Boolean = false
    ): Directory? {
        val token = authService.getAuthToken()

        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Files/CreateDirectory")
                    .queryParam("processId", processID)
                    .queryParam("expiresInMins", expiresInMins)
                    .queryParam("isUpload", isUpload)
                    .build()
            }
            .headers { it.setBearerAuth(token) }

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
        val token = authService.getAuthToken()

        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Files/GetObservationFiles")
                    .queryParam("processId", processID)
                    .queryParam("directoryId", directoryID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("filePattern", "**/*.*")
                    .build()
            }
            .headers { it.setBearerAuth(token) }

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
        return blobClient.downloadStream(outputStream)
    }

    fun downloadResourceFile(url: String) {

    }

    // Get Observation Files....
    fun getObservationsV3(
        processID: String,
        startObsIndex: String,
        endObsIndex: String,
        expiresInMin: String = "60",
//        authorizedClient: OAuth2AuthorizedClient?
    ): EgressResult? {
        val token = authService.getAuthToken()

        apiVersion = "/v3"
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Files/GetObservations")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("expiresInMins", expiresInMin)
                    .build()
            }
            .headers { it.setBearerAuth(token) }

//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }
            .retrieve()
            .bodyToMono(EgressResult::class.java)
        val data = response.share().block()
        val sasUrlList: ArrayList<String> = ArrayList<String>()
        data?.files?.forEach { it -> sasUrlList.add(it.sasUrl) }
        logger.debug(data.toString())
        return data

//        return sasUrlList

    }

    fun getObservation(
        processID: String,
        startObsIndex: String,
        version: String,
//        authorizedClient: OAuth2AuthorizedClient?
    ): UploadObservationObject? {
        val token = authService.getAuthToken()

        apiVersion = "/v2"
        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Process/GetObservation")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("version", version)
                    .build()
            }
            .headers { it.setBearerAuth(token) }

//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }
            .retrieve()
            .bodyToMono(UploadObservationObject::class.java)
        val data = response.share().block()
//        val sasUrlList:ArrayList<String> =ArrayList<String>()
//        data?.dataLakeFiles?.forEach {it -> sasUrlList.add(it.sasUrl)}
        return data
    }

    fun getTransferStat(
        processID: String,
        directoryID: String,
        transferId: String,
    ): TransferStat? {
        val token = authService.getAuthToken()
        apiVersion = "/v2"

        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("$apiVersion/Files/GetTransferState")
                    .queryParam("processId", processID)
                    .queryParam("directoryId", directoryID)
                    .queryParam("transferId", transferId)
                    .build()
            }
            .headers { it.setBearerAuth(token) }

            .retrieve()
            .bodyToMono(TransferStat::class.java)
        val transString = response.share().block()
        return transString
    }

    fun getObservationFilesV3(
        processID: String,
        startObsIndex: String,
        endObsIndex: String,
        expiresInMins: String
    ): EgressResult? {
        val token = authService.getAuthToken()

        apiVersion = "/v3"
        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Files/GetObservationFiles")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("expiresInMins", expiresInMins)
                    .queryParam("filePattern", "**/*.*")
                    .build()
            }
            .headers { it.setBearerAuth(token) }

            .retrieve()
            .bodyToMono(EgressResult::class.java)

        val result = response.share().block()
        return result
    }


    fun listDirectories(processID: String, isUpload: String): String? {
        apiVersion = "/v2"
        val token = authService.getAuthToken()

        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Files/ListDirectories")
                    .queryParam("processId", processID)
                    .queryParam("isUpload", isUpload)
                    .build()
            }
            .headers { it.setBearerAuth(token) }

//            .headers { header -> header.setBearerAuth(authorizedClient?.accessToken?.tokenValue.toString()) }
            .retrieve()
            .bodyToMono(String::class.java)
        val transferId = response.share().block().toString()
        return transferId
    }

    fun putObservationFiles(
        processID: String,
        directoryId: String,
        startObsIndex: String,
        endObsIndex: String,
        version: String,
        dataFiles: List<String>
//                            authorizedClient: OAuth2AuthorizedClient?
    ): String {
        apiVersion = "/v2"
        val token = authService.getAuthToken()

        val response = webClient
            .put()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Files/PutObservationFiles")
                    .queryParam("processId", processID)
                    .queryParam("directoryId", directoryId)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("endObsIndex", endObsIndex)
                    .queryParam("version", version)
                    .build()
            }
            .headers { it.setBearerAuth(token) }

            .bodyValue(dataFiles)
            .retrieve()
            .bodyToMono(String::class.java)
        val transferId = response.share().block().toString()
        return transferId
    }

    fun appendVersions(observationObject: UploadObservationObject): String? {
        val token = authService.getAuthToken()

        apiVersion = "/v2"
        val response = webClient
            .post()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Process/AppendVersion")
                    .build()
            }
            .headers { it.setBearerAuth(token) }

            .body(BodyInserters.fromPublisher(Mono.just(observationObject), UploadObservationObject::class.java))
            .retrieve()
            .bodyToMono(String::class.java)
        val result = response.share().block().toString()
        return result
    }

    fun DownloadFiles(values: EgressResult, dir: String) {
        var fileDownLoadMap: HashMap<String, String> = HashMap<String, String>()
        values.files?.forEach {
            fileDownLoadMap.put(it.name, it.sasUrl)
        }


        val downloadDir = when (Paths.get(dir).isAbsolute) {
            false -> Paths.get(dir).toAbsolutePath().normalize()
            else -> Paths.get(dir)
        }


//        val path = Paths.get("").toAbsolutePath()

//        val outputDir = "$path/outputFile/"

//        if (Files.notExists(Paths.get(outputDir)))
//            Files.createDirectories(Paths.get(outputDir))

        if (Files.notExists(downloadDir))
            Files.createDirectories(downloadDir)

        //            logger.info(fileDownLoadMap.entries)
//        fileDownLoadMap.filter { it.key == "dat/0/col_source/dataset/120.xml" }
//        logger.info(fileDownLoadMap.size)


        // Here we need to check the status of the transfer ID

//        Thread.sleep(30000)

        fileDownLoadMap.entries.forEach {
            val fname = it.key
            val url = it.value
            val filePath = "$downloadDir/${fname}"

            val tmpDir = Paths.get(filePath).parent

            if (Files.notExists(tmpDir))
                Files.createDirectories(tmpDir)

            FileDownloader.get(url, filePath)
        }
        println("Download Operation Completed")
    }


    fun getPeekObservations(
        processID: String,
        startObsIndex: String,
        version: String,
        maxReturn: Int = 500,
        isForwardDir: Boolean = true
    ): PeekObservationResult? {
        val token = authService.getAuthToken()

        apiVersion = "/v2"
        val response = webClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(apiVersion + "/Process/PeekObservations")
                    .queryParam("processId", processID)
                    .queryParam("obsIndex", startObsIndex)
                    .queryParam("version", version)
                    .queryParam("maxReturn",maxReturn)
                    .queryParam("isForwardDir",isForwardDir)
                    .build()
            }
            .headers { it.setBearerAuth(token) }
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError) { Mono.error(RuntimeException("4XX Error ${it.statusCode()}, ${it.bodyToMono(String::class.java)}")) }
            .onStatus(HttpStatus::is5xxServerError) { Mono.error(RuntimeException("5XX Error ${it.statusCode()}, ${it.bodyToMono(String::class.java)}")) }
            .bodyToMono(PeekObservationResult::class.java)
        val result = response.share().block()
        return result
    }

    fun getAllPeekObservations(
        processID: String,
        endObsIndex: String,
        version: String = "0",
    ): PeekObservationResult? {
        var tmpResult = mutableListOf<UploadObservationObject>()

        if (endObsIndex.toInt() < 500) {
            return getPeekObservations(
                processID = processID,
                startObsIndex = "0",
                maxReturn = 500,
                version = version
            )
        }
        else{
            var remainingCount = endObsIndex.toInt()
            var maxResult = 0
            var startNumber = 0
            while (remainingCount>0){
                if (remainingCount>=500) {
                    maxResult = 500
                    remainingCount =-500
                }else
                {
                    maxResult = remainingCount
                    remainingCount =0
                }
                var tmpReturn =  getPeekObservations(
                    processID = processID,
                    startObsIndex = startNumber.toString(),
                    maxReturn = maxResult,
                    version = version
                )
                tmpReturn?.let { tmpResult.addAll(it) }
                startNumber += min(remainingCount, 500)
            }
        }
        return tmpResult.toTypedArray() as PeekObservationResult
    }
}