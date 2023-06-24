package common.services


import com.azure.storage.blob.BlobClientBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import common.model.ContentContent
import common.model.TaxonomyContentType
import common.model.observation.TaxonomyData
import common.model.taxonomyserver.AppendMetadata
import common.model.taxonomyserver.AppendResponse
import common.services.auth.AuthService
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.io.IOException
import java.io.Serializable
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Logger
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Service
//Service for retrieving taxonomy-related content information from the WebGME instance.
class TaxonomyInfoService(
    @Qualifier("WebGMEWebClient")
    val webClient: WebClient,
    val authServiceObj: AuthService,
) {
    var aadToken: String = ""
    var webgmeAccesstoken: String = ""
    var encodedProjectID: String = ""
    var encodedProjectBranch: String = ""
    var taxonomyContentTypeInfo: TaxonomyContentType? = null
    var contentRepoMap: Map<String, ContentContent>? = null

    var logger = Logger.getLogger(TaxonomyInfoService::class.java.name)

    fun getTokens() {
        aadToken = authServiceObj.getAuthToken()
        webgmeAccesstoken = getWebGMEToken().toString()
    }

    fun getCookie(): String {
//        return "webgme_aad=$aadToken; access_token=$webgmeAccesstoken ;"
        return "udcp_taxonomy_aad=$aadToken; access_token=$webgmeAccesstoken ;"
    }

    fun initTaxonomyInfoService(
        encodedProjectID: String,
        encodedProjectBranch: String,
    ) {
        this.encodedProjectID = encodedProjectID
        this.encodedProjectBranch = encodedProjectBranch
        getTokens()
        this.taxonomyContentTypeInfo = getTaxonomyContentsInfo()
//        this.contentRepoMap = getContentRepoMap()
    }

    private fun getTaxonomyContentsInfo(
    ): TaxonomyContentType? {
        val finalCookie = getCookie()

        //  https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Dashboard/AllLeap%2BTaxonomyBootcamp/branch/master/static/index.html
        val response = webClient.get()
            .uri { uriBuilder: UriBuilder ->
                UriComponentsBuilder.fromUri(uriBuilder.build())
                    //                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
                    .path("routers/Dashboard/{encodedProjectID}/branch/{encodedProjectBranch}/info")
                    .encode()
                    .buildAndExpand(encodedProjectID, encodedProjectBranch)
                    //                .buildAndExpand(guidTagsEncoded)
                    .toUri()
            }
            .header(HttpHeaders.COOKIE, finalCookie)
            .retrieve()
            .bodyToMono(TaxonomyContentType::class.java)
            .block()
        return response
    }



    suspend fun downloadFile(
        repositoryID: String,
        index: String,
        contentTypePath: String,
        dir: String
    ) {
        val finalCookie = getCookie()
        try {
            val dirPath = Paths.get(dir)
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath)
            }
            var fileName = ""

            val response = webClient.
            get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        //                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
                        .path("routers/Search/{encodedProjectID}/branch/{encodedProjectBranch}/{contentTypePath}/artifacts/{repositoryID}/download")
                        .queryParam("ids", "{index}")
                        .encode()
                        .build(false)
                        .expand(encodedProjectID, encodedProjectBranch, contentTypePath, repositoryID, index)
                        .toUri()
                }
                .header(HttpHeaders.COOKIE, finalCookie)
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful) {
                    clientResponse ->
                    clientResponse.headers().asHttpHeaders().contentDisposition.filename?.let {
                        fileName = it
                    }
                    when(fileName)
                    {
                        "" -> fileName = UUID.randomUUID().toString()
                    }
                      println("Filename: $fileName")
                    Mono.empty()
                }
                .bodyToFlux(DataBuffer::class.java)

//            println("Exiting out of downloadFile")
            when(fileName)
            {
                "" -> fileName = UUID.randomUUID().toString()
            }
            val destination = Paths.get(dir, fileName)
            DataBufferUtils.write(response, destination).block()
            var renamedPath: Path = Paths.get(dir, fileName)

            // check if the renamedPath exists and if it does, then append the filename with an increasing number
            if (Files.exists(renamedPath)) {
                val fileNameWithoutExtension = fileName.substringBeforeLast(".")
                val fileExtension = fileName.substringAfterLast(".")
                var i = 1
                while (Files.exists(renamedPath)) {
                    fileName = "$fileNameWithoutExtension($i).$fileExtension"
                    renamedPath = Paths.get(dir, fileName)
                    i++
                }
            }
            Files.move(destination, renamedPath, StandardCopyOption.REPLACE_EXISTING)
            println("Downloaded $renamedPath")
        } catch (e: Exception) {
            println("Error in downloading file: $e")
        }
    }


    private fun fetchContentRepoMap(
        requestedContentType: List<String> = listOf(),
    ): HashMap<String, ContentContent>? {
        val finalCookie = getCookie()
        var contentTypeURLPair = taxonomyContentTypeInfo?.contentTypes?.map {
            Pair(
                it.name,
                it.url.subSequence(0, it.url.lastIndexOf("static")).toString() + "artifacts/"
            )
        }
        var combinedresult: HashMap<String, ContentContent>? = hashMapOf()



        runBlocking {
            val results = contentTypeURLPair?.mapNotNull {
//
//                val artifactResponse = getContentRecordRequest(it.second, finalCookie)
//                println(artifactResponse)



                if (it.first in requestedContentType || requestedContentType.isEmpty()) {
                    async{
                        val artifactResponse = getContentRecordRequest(it.second, finalCookie)
//                        println("Artifact Response: $artifactResponse")
//                        println("Artifact Response: ${it.first to artifactResponse}")
                        it.first to artifactResponse

                    }
                } else {
                    null
                }
            }
            val tmp = results?.awaitAll() ?: emptyList()
//            val tmp = results?: emptyList()
            tmp.forEach { (contentType, content) ->
                content?.let { combinedresult?.set(contentType, it) }
            }
        }
        return combinedresult
    }

    /**
     * Sends a GET request to retrieve content information from a given [contentURI] using a specified [finalCookie].
     *
     * @param contentURI The URI for the content information.
     * @param finalCookie The cookie used for authentication.
     * @return The information about the content in [ContentContent] format, or null if no content is found.
     */
  private suspend fun getContentRecordRequest(contentURI: String, finalCookie: String): ContentContent? {
//    private fun getContentRecordRequest(contentURI: String, finalCookie: String): ContentContent? {
        //        https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Search/AllLeap%2BTaxonomyBootcamp/branch/master/%2FH/artifacts/
//      println("Content URI: $contentURI")
//        println("Final Cookie: $finalCookie")
      val artifactResponse =
            webClient.get()
                //                            .uri(it.second.encode())
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        .path(contentURI).encode()
                        .build(true).toUri()
                }
                .header(HttpHeaders.COOKIE, finalCookie)
                .retrieve()
                .bodyToMono(ContentContent::class.java)
//                .bodyToMono(String::class.java)
                .awaitSingle()

//        println(artifactResponse)
        return artifactResponse as ContentContent
    }

    fun getDownloadURL(processID: String, index: String): String {
//        val encodedProjectID = "AllLeap+TaxonomyBootcamp"
//        val encodedProjectBranch = "master"

        val contentTypeName = getContentTypeOfRepository(processID)
        val contentTypePath = getPathofContentType(contentTypeName)
//        println("Content Type Path: $contentTypePath" + "Content Type Name: $contentTypeName")


//            val downloadURL = "https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Dashboard/$encodedProjectID/branch/$encodedProjectBranch/$contentTypeRef/$processID/download?ids=$index"
        val downloadURL =
            "/routers/Dashboard/$encodedProjectID/branch/$encodedProjectBranch/$contentTypePath/$processID/download?ids=$index"
        return downloadURL
    }

    fun getProcessIDinfo(processID: String): String {
        val contentTypeRef =
            taxonomyContentTypeInfo?.contentTypes?.find { it.name == processID }?.url?.split("/")?.get(6)
        // this.taxonomyContentTypeInfo.contentTypes.find { it.name == "Demo" }.path
        return contentTypeRef ?: ""
    }

    fun getWebGMEToken(): String? {
        val response = webClient.get()
            .uri("/aad/device")
            .headers { it.setBearerAuth(aadToken) }
            .accept(MediaType.ALL)
            .exchange().block()
//            .retrieve()
//            .bodyToMono(String::class.java)
//            .block()
//            println(response)
        val access_token = response?.cookies()?.get("access_token")?.get(0)?.value
        return access_token
    }

    // Create a test function to get the list of Repositories
    fun getListofRepositories(requestedContentType: List<String> = listOf()): Serializable {
        if (contentRepoMap == null) {
            contentRepoMap = fetchContentRepoMap(requestedContentType)
        }
        // This gives list of repositories for a given ContentType
        val listofContentTypes = contentRepoMap?.keys?.toList()
        // list of repositories for a given content type
        var tmpcontentRepoMap: HashMap<String, List<Pair<String, String>>> = hashMapOf()

        listofContentTypes?.map { it ->
            tmpcontentRepoMap[it] =
                contentRepoMap?.get(it)?.map { Pair(it.id, it.displayName) }?.toList() as List<Pair<String, String>>
        }

        return tmpcontentRepoMap
    }

    fun getContentTypeOfRepository(repositoryID: String = "87dc1607-5d63-4073-9424-720f86ecef43"): String {
        if (contentRepoMap == null) {
            contentRepoMap = fetchContentRepoMap()
        }
        val contentType = contentRepoMap?.filterValues { it.any { it.id == repositoryID } }?.keys?.firstOrNull()

        return contentType ?: ""
    }


    fun getPathofContentType(contentType: String = "Demo") : String{
        if (taxonomyContentTypeInfo == null) {
            taxonomyContentTypeInfo = getTaxonomyContentsInfo()
        }
        val path = taxonomyContentTypeInfo?.contentTypes?.find { it.name == contentType }?.path
        return path ?: ""
    }

    @OptIn(ExperimentalTime::class)
    fun uploadToRepository(repositoryID: String, uploadDir: Path, metadataFilePath: Path?, displayName : String? = null) {
        val observationMapper = jacksonObjectMapper()
        var rawData: TaxonomyData? = null
        // Read the metadata file
        metadataFilePath?.let {
            rawData = observationMapper.readValue(it.toFile(), TaxonomyData::class.java)
        }

        displayName?.let {
            rawData?.displayName = displayName
        }
        rawData?.displayName = rawData?.displayName ?: "Submission ${LocalDateTime.now()}"
        val fileinfo = FileUploader.getMapofRelativeAndAbsolutePath(uploadDir.toString())
        val tmpdirUUID = UUID.randomUUID().toString()

//        val listofFiles = fileinfo.keys.map{ "$tmpdirUUID/$it" }.toList()
        // FIX ME! Here we could remove the tmpdirUUID from the list of files
        val listofFiles = fileinfo.keys.map { "$it" }.toList()

        var tmpAppendMetadata = AppendMetadata(filenames = listofFiles, metadata = rawData!!)
//        println(tmpAppendMetadata)

        var response =
            appendMetadataToRepository(repositoryID, tmpAppendMetadata)

        val tmpurlInfo = jacksonObjectMapper().readValue(response, AppendResponse::class.java).appendFiles.associate {
            Pair(
                it.name.split("/").last(),
                it.params.url
            )
        }
        // FIX ME: Here we need to get the new folder structure as a response from the appendMetadataToRepository call
        // and use that to upload the files
//        val tmpMap = fileinfo.keys.map { "$tmpdirUUID/$it" to it }.toMap()

        val tmpMap = fileinfo.keys.map { "$it" to it }.toMap()

        val measureTime = measureTime {
            val MAX_CONCURRENT_UPLOADS = 10
            runBlocking {
                val uploadContext = Dispatchers.IO + CoroutineName("UploadCoroutine")
                val uploadSemaphore = Semaphore(MAX_CONCURRENT_UPLOADS)
                val deferredUploads = tmpMap.keys.map { key ->
                    val tmpurl = tmpurlInfo[key]
                    val tmpfile = tmpMap[key]
                    val filePath = fileinfo[tmpfile]
                    async(uploadContext) {
                        if (tmpurl != null) {
                            uploadSemaphore.withPermit {
                                try {
                                    logger.info("Uploading $filePath to $tmpurl")
                                    uploadBlob(tmpurl, filePath.toString())
                                } catch (ex: IOException) {
                                    logger.info("Failed to upload $filePath: ${ex.message}")
                                }
                            }
                        }
                    }
                }
                deferredUploads.forEach { it.await() }
            }
        }
        logger.info("Time taken to upload ${fileinfo.size} files is $measureTime")
    }

    suspend fun uploadBlob(sasUrl: String, uploadDir: String) {
        if (Files.notExists(Paths.get(uploadDir))) {
            logger.info("Folder does not exist")
            return
        }

        try {
            logger.info("Uploading file: $uploadDir to $sasUrl")
            var blobclient = BlobClientBuilder().endpoint(sasUrl).buildClient().uploadFromFile(uploadDir, true)
            logger.info("Finished Uploading $uploadDir with response ${blobclient.toString()}")
        } catch (ex: UncheckedIOException) {
            logger.info("Failed to upload from file ${ex.message}")
        }
    }



        // Upload the files to the repository

    fun appendMetadataToRepository(repositoryID: String, content: Any) : String {
        val contentTypeName = getContentTypeOfRepository(repositoryID)
        val contentTypePath = getPathofContentType(contentTypeName)
        val finalCookie = getCookie()

//        println("Final Cookie: $finalCookie")
//
//        println("Content Type Path: $contentTypePath  " + "Content Type Name: $contentTypeName")
//        // add content to the body of the request
//        println("Content: $content")


        val response = webClient.post()
            .uri { uriBuilder: UriBuilder ->
                UriComponentsBuilder.fromUri(uriBuilder.build())
                    .path("/routers/Search/{encodedProjectID}/branch/{encodedProjectBranch}/{contentTypePath}/artifacts/{repositoryID}/append")
                    .encode()
                    .buildAndExpand(encodedProjectID, encodedProjectBranch, contentTypePath, repositoryID)
                    .toUri()
            }
            .header(HttpHeaders.COOKIE, finalCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(content)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        return response!!
    }

}
