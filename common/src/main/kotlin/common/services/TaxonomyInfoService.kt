package common.services

import com.azure.storage.blob.BlobClientBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import common.model.ContentContent
import common.model.RepositoryList
import common.model.TaxonomyContentType
import common.model.observation.FileUrlInfo
import common.model.observation.TaxonomyData
import common.model.taxonomyserver.AppendMetadata
import common.model.taxonomyserver.AppendResponse
import common.services.auth.AuthService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineName

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.UUID
import java.util.logging.Logger
import java.util.zip.ZipInputStream
import kotlin.collections.HashMap
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

// Service for retrieving taxonomy-related content information from the WebGME instance.
@Service
class TaxonomyInfoService(
    @Qualifier("WebGMEWebClient")
    val webClient: WebClient,
    val authServiceObj: AuthService,
) {
    var aadToken: String = ""
    var webgmeAccesstoken: String = ""
//    var encodedProjectID: String = ""

    @Value("\${cliclient.TaxonomyServer.ProjectTypeValue:main}")
    lateinit var encodedProjectTypeValue: String

    @Value("\${cliclient.TaxonomyServer.CookieName:udcp_taxonomy_aad}")
    lateinit var encodedCookieName: String

    var taxonomyContentTypeInfo: TaxonomyContentType? = null
    var contentRepoMap: Map<String, RepositoryList>? = null

    var logger = Logger.getLogger(TaxonomyInfoService::class.java.name)

    @Value("\${cliclient.TaxonomyServer.ProjectID:AllLeap+TaxonomyBootcamp}")
    lateinit var encodedProjectID: String

    @Value("\${cliclient.TaxonomyServer.ProjectType:branch}")
    lateinit var encodedProjectType: String

//    @Value("\${cliclient.TaxonomyServer.ProjectTypeValue:main}")
//    lateinit var encodedProjectTypeValue: String

    fun getTokens() {
        aadToken = authServiceObj.getAuthToken()
        // logger.debug("aadToken: $aadToken")
        if (aadToken == "") {
            throw Exception("AAD token is empty, If using Passthrough, Make sure to set the AAD token using -t option")
        }
        webgmeAccesstoken = getWebGMEToken().toString()
        if (webgmeAccesstoken == "") {
            throw Exception("WebGME token is empty")
        }

//        logger.info("webgmeAccesstoken: $webgmeAccesstoken")
    }

    fun getCookie(): String {
        return "$encodedCookieName=$aadToken; access_token=$webgmeAccesstoken ;"
    }

    fun initTaxonomyInfoService() {
        getTokens()
        this.taxonomyContentTypeInfo = getTaxonomyContentsInfo()
    }

    fun getContentTypeJsonSchema(repositoryID: String): Pair<String, String> {
        val contentTypeName = getContentTypeOfRepository(repositoryID)
        val contentTypePath = getPathofContentType(contentTypeName)
        val finalCookie = getCookie()
        // https://wellcomewebgme.centralus.cloudapp.azure.com/routers/JSONSchema/AllLeap%2BMCPyschTaxonomy/tag/1_1_0/%2FH%2F6/schema.json?onlyReleased=true
        val contentTypeRepoConfig = getContentRepoConfiguration(contentTypePath)
        // use the configuration to fetch the JSON value of the content key
        val contentTypeRepoConfigObj = jacksonObjectMapper().readTree(contentTypeRepoConfig)
        val contentKey = contentTypeRepoConfigObj.get("content").get("content")["nodePath"].asText()
//        println("Content Key: $contentKey")

        val project = contentTypeRepoConfigObj.get("project")
        println("TaxonomyVersion: $project")
        val jsonschemaResponse = getJSONSchemaForContentRepoRequest(contentKey)
        return Pair(jsonschemaResponse, project.toString())
    }

    private fun getContentRepoConfiguration(contentTypePath: String): String? {
        // https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Search/AllLeap%2BMCPyschTaxonomy/branch/master/%2FH/configuration.json
        val finalCookie = getCookie()
        val response =
            webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        .path(
                            "routers/Search/{encodedProjectID}/{encodedProjectType}/{encodedProjectTypeValue}/{contentTypePath}/configuration.json",
                        )
                        .encode()
                        .buildAndExpand(encodedProjectID, encodedProjectType, this.encodedProjectTypeValue, contentTypePath)
                        .toUri()
                }
                .header(HttpHeaders.COOKIE, finalCookie)
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
        return response
    }

    private fun getJSONSchemaForContentRepoRequest(contentTypeRepoPath: String): String {
        val finalCookie = getCookie()

//        println("Content Repo Path: $contentTypeRepoPath")

        try {
            val response =
                webClient.get()
                    .uri { uriBuilder: UriBuilder ->
                        UriComponentsBuilder.fromUri(uriBuilder.build())
                            .path(
                                "routers/JSONSchema/{encodedProjectID}/{encodedProjectType}" +
                                    "/{encodedProjectTypeValue}/{contentTypeRepoPath}/schema.json",
                            )
                            .queryParam("onlyReleased", "true")
                            .encode()
                            .buildAndExpand(
                                encodedProjectID,
                                encodedProjectType,
                                this.encodedProjectTypeValue,
                                contentTypeRepoPath,
                            )
                            .toUri()
                    }
                    .header(HttpHeaders.COOKIE, finalCookie)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .block()
            return response.toString()
        } catch (Exception: Exception) {
            throw Exception("Error in getting JSON Schema for the Content Repo $Exception")
        }
    }

    private fun getTaxonomyContentsInfo(): TaxonomyContentType? {
        val finalCookie = getCookie()
        try {
            val response =
                webClient.get()
                    .uri { uriBuilder: UriBuilder ->
                        UriComponentsBuilder.fromUri(uriBuilder.build())
                            //                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
                            .path(
                                "routers/Dashboard/{encodedProjectID}/{encodedProjectType}/" +
                                    "{encodedProjectTypeValue}/info",
                            )
                            .encode()
                            .buildAndExpand(encodedProjectID, encodedProjectType, this.encodedProjectTypeValue)
                            //                .buildAndExpand(guidTagsEncoded)
                            .toUri()
                    }
                    .header(HttpHeaders.COOKIE, finalCookie)
//            .accept(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .bodyToMono(TaxonomyContentType::class.java)
//                .bodyToMono(String::class.java)
                    .block()
//            println(response)
            return response as TaxonomyContentType
        } catch (e: Exception) {
            throw Exception("Need to Login to UDCP WebPortal First Taxonomy DesignStudio First")
        }
    }

    suspend fun downloadFileUrls(
        repositoryID: String,
        index: String,
        contentTypePath: String,
    ): FileUrlInfo? {
        val finalCookie = getCookie()
        val response =
            webClient
                .get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        //                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
                        .path(
                            "routers/Search/{encodedProjectID}/{encodeProjectType}/{encodedProjectTypeValue}/" +
                                "{contentTypePath}/artifacts/{repositoryID}/files",
                        )
                        .queryParam("ids", "{index}")
                        .encode()
                        .build(false)
                        .expand(
                            encodedProjectID,
                            encodedProjectType,
                            this.encodedProjectTypeValue,
                            contentTypePath,
                            repositoryID,
                            index,
                        )
                        .toUri()
                }
                .header(HttpHeaders.COOKIE, finalCookie)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(String::class.java)
                .awaitSingle()

        val fileUrlInfo = jacksonObjectMapper().readValue(response, FileUrlInfo::class.java)
        return fileUrlInfo
    }

    suspend fun getMetadata(
        repositoryID: String,
        index: String,
        contentTypePath: String,
    ): TaxonomyData? {
        val finalCookie = getCookie()
        val response =
            webClient
                .get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        .path(
                            "routers/Search/{encodedProjectID}/{encodedProjectType}/{encodedProjectTypeValue}/" +
                                "{contentTypePath}/artifacts/{repositoryID}/{index}/metadata.json",
                        )
                        .encode()
                        .build(false)
                        .expand(
                            encodedProjectID,
                            encodedProjectType,
                            this.encodedProjectTypeValue,
                            contentTypePath,
                            repositoryID,
                            index,
                        )
                        .toUri()
                }
                .header(HttpHeaders.COOKIE, finalCookie)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TaxonomyData::class.java)
                .awaitSingle()
        return response
    }

    suspend fun saveMetadataFile(
        repositoryID: String,
        index: String,
        contentTypePath: String,
        dir: String,
    ): Unit? {
        val metadataObj = getMetadata(repositoryID, index, contentTypePath)
        if (metadataObj != null) {
            val observationMapper = jacksonObjectMapper()
            val metadataFilePath = Paths.get(dir, "metadata.json")
            val metadataFile = File(metadataFilePath.toString())
            observationMapper.writeValue(metadataFile, metadataObj)
        }
        return Unit
    }

    suspend fun DownloadFiles(
        values: FileUrlInfo,
        dir: String,
    ) {
        val fileDownLoadMap: HashMap<String, String> = HashMap<String, String>()
        values.forEach {
            it.files.forEach {
                fileDownLoadMap.put(it.name, it.url)
            }
        }

        val downloadDir =
            when (Paths.get(dir).isAbsolute) {
                false -> Paths.get(dir).toAbsolutePath().normalize()
                else -> Paths.get(dir)
            }

        if (Files.notExists(downloadDir)) {
            withContext(Dispatchers.IO) {
                Files.createDirectories(downloadDir)
            }
        }
        coroutineScope {
            fileDownLoadMap.map { file ->
                async(Dispatchers.IO) {
                    println("Downloading ${file.key} from ${file.value}")
                    val filePath = "$downloadDir/${file.key}"
                    val tmpDir = Paths.get(filePath).parent
                    if (Files.notExists(tmpDir)) {
                        Files.createDirectories(tmpDir)
                    }
                    FileDownloader.downloadFile(file.value, filePath)
                }
            }.awaitAll()
        }
    }

    suspend fun downloadFile(
        repositoryID: String,
        index: String,
        contentTypePath: String,
        dir: String,
    ) {
        val finalCookie = getCookie()
        try {
            val dirPath = Paths.get(dir)
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath)
            }
            var fileName = ""
            val response =
                webClient
                    .get()
                    .uri { uriBuilder: UriBuilder ->
                        UriComponentsBuilder.fromUri(uriBuilder.build())
                            //                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
                            .path(
                                "routers/Search/{encodedProjectID}/{encodedProjectType}/" +
                                    "{encodedProjectTypeValue}/{contentTypePath}/artifacts/{repositoryID}/download",
                            )
                            .queryParam("ids", "{index}")
                            .encode()
                            .build(false)
                            .expand(
                                encodedProjectID,
                                encodedProjectType,
                                this.encodedProjectTypeValue,
                                contentTypePath,
                                repositoryID,
                                index,
                            )
                            .toUri()
                    }
                    .header(HttpHeaders.COOKIE, finalCookie)
                    .retrieve()
                    .onStatus(HttpStatusCode::is2xxSuccessful) { clientResponse ->
                        clientResponse.headers().asHttpHeaders().contentDisposition.filename?.let {
                            fileName = it
                        }
                        when (fileName) {
                            "" -> fileName = UUID.randomUUID().toString()
                        }
//                      println("Filename: $fileName")
                        Mono.empty()
                    }
                    .bodyToFlux(DataBuffer::class.java)

//            println("Exiting out of downloadFile")
            when (fileName) {
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
            // unzip the file if it is a zip file
            if (fileName.endsWith(".zip")) {
                extractZipFile(renamedPath.toString(), dir)
                Files.delete(renamedPath)
                println("Downloaded to $dir")
            } else {
                println("Downloaded $renamedPath")
            }
        } catch (e: Exception) {
            println("Error in downloading file: $e")
        }
    }

    // Derived from: https://www.digitalocean.com/community/tutorials/java-unzip-file-example
    fun extractZipFile(
        zipFilePath: String,
        destinationFolderPath: String,
    ) {
        val buffer = ByteArray(1024)
        val zipInputStream = ZipInputStream(FileInputStream(zipFilePath))
        var zipEntry = zipInputStream.nextEntry

        while (zipEntry != null) {
            val entryFilePath = destinationFolderPath + File.separator + zipEntry.name
            val entryFile = File(entryFilePath)

            // Create directories if necessary
            if (zipEntry.isDirectory) {
                entryFile.mkdirs()
            } else {
                // Create parent directories if necessary
                entryFile.parentFile?.mkdirs()

                // Extract the file
                val fileOutputStream = FileOutputStream(entryFile)
                var length = zipInputStream.read(buffer)

                while (length >= 0) {
                    fileOutputStream.write(buffer, 0, length)
                    length = zipInputStream.read(buffer)
                }

                fileOutputStream.close()
            }

            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()
    }

    private fun fetchContentRepoMap(requestedContentType: List<String> = listOf()): HashMap<String, RepositoryList>? {
        val finalCookie = getCookie()
        var contentTypeURLPair =
            taxonomyContentTypeInfo?.contentTypes?.map {
                Pair(
                    it.name,
                    it.path,
//                "routers/Search/${this.encodedProjectID}/branch/${this.encodedProjectBranch}/${it.path}/artifacts/"
//                it.url.subSequence(0, it.url.lastIndexOf("static")).toString() + "artifacts/"
                )
            }

        logger.info("ContentType URL Pair: $contentTypeURLPair")

        var combinedresult: HashMap<String, RepositoryList>? = hashMapOf()

        runBlocking {
            val results =
                contentTypeURLPair?.mapNotNull {
//
//                val artifactResponse = getContentRecordRequest(it.second, finalCookie)
//                println(artifactResponse)

                    if (it.first in requestedContentType || requestedContentType.isEmpty()) {
                        async {
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
            tmp.forEach { (contentType, repoList) ->
                repoList?.let { combinedresult?.set(contentType, it) }
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
    private suspend fun getContentRecordRequest(
        contentURI: String,
        finalCookie: String,
    ): RepositoryList? {
//    private fun getContentRecordRequest(contentURI: String, finalCookie: String): ContentContent? {
        //        https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Search/AllLeap%2BTaxonomyBootcamp/branch/master/%2FH/artifacts/
//      println("Content URI: $contentURI")
//        println("Final Cookie: $finalCookie")
        val artifactResponse =
            webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        .path(
                            "/routers/Search/{encodedProjectID}/{encodedProjectType}" +
                                "/{encodedProjectTypeValue}/{contentURI}/artifacts/",
                        )
                        .encode()
                        .buildAndExpand(encodedProjectID, encodedProjectType, this.encodedProjectTypeValue, contentURI)
                        .toUri()
                }
                .header(HttpHeaders.COOKIE, finalCookie)
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(RepositoryList::class.java)
                .timeout(java.time.Duration.ofSeconds(50))
                .awaitSingle()

//        println(artifactResponse)
//        return artifactResponse as ContentContent
        return artifactResponse as RepositoryList
    }

    fun getDownloadURL(
        processID: String,
        index: String,
    ): String {
//        val encodedProjectID = "AllLeap+TaxonomyBootcamp"
//        val encodedProjectBranch = "master"

        val contentTypeName = getContentTypeOfRepository(processID)
        val contentTypePath = getPathofContentType(contentTypeName)
//        println("Content Type Path: $contentTypePath" + "Content Type Name: $contentTypeName")

//            val downloadURL = "https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Dashboard/$encodedProjectID/branch/$encodedProjectBranch/$contentTypeRef/$processID/download?ids=$index"
        val downloadURL =
            "/routers/Dashboard/$encodedProjectID/$encodedProjectType/" +
                "${this.encodedProjectTypeValue}/$contentTypePath/artifacts/$processID/$index/metadata.json"
        return downloadURL
    }

    fun getWebGMEToken(): String? {
        // catch any exception while connecting to the taxonomy server.
        // If there is an exception, then we need to login to the taxonomy server first

        var access_token: String? = null
        try {
            val response =
                webClient.get()
                    .uri("/aad/device")
                    .headers { it.setBearerAuth(aadToken) }
                    .accept(MediaType.ALL)
                    .exchange().block()
            access_token = response?.cookies()?.get("access_token")?.get(0)?.value
        } catch (e: Exception) {
            println(
                "Error in Connecting to the Taxonomy Server... " +
                    "Please make sure you have internet connection",
            )
            throw e
        }
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

    fun getPathofContentType(contentType: String = "Demo"): String {
        if (taxonomyContentTypeInfo == null) {
            taxonomyContentTypeInfo = getTaxonomyContentsInfo()
        }
        val path = taxonomyContentTypeInfo?.contentTypes?.find { it.name == contentType }?.path
        return path ?: ""
    }

    @OptIn(ExperimentalTime::class)
    fun uploadToRepository(
        repositoryID: String,
        uploadDir: Path,
        metadataFilePath: Path?,
        displayName: String? = null,
    ) {
        val observationMapper = jacksonObjectMapper()
        var rawData: TaxonomyData? = null
        // Read the metadata file
        // set the rawData to the TaxonomyData class based on the metadatafilePath.
        rawData =
            if (metadataFilePath != null) {
                observationMapper.readValue(metadataFilePath.toFile(), TaxonomyData::class.java)
            } else {
                TaxonomyData("Submission ${LocalDateTime.now()}")
            }

        displayName?.let {
            rawData?.displayName = displayName
        }
        rawData?.displayName = rawData?.displayName ?: "Submission ${LocalDateTime.now()}"
        val fileinfo = FileUploader.getMapofRelativeAndAbsolutePath(uploadDir.toString())
        // FIX ME! Here we could remove the tmpdirUUID from the list of files
        val listofFiles = fileinfo.keys.map { "$it" }.toList()

        val tmpAppendMetadata = AppendMetadata(filenames = listofFiles, metadata = rawData!!)
//        println(tmpAppendMetadata)

        val response =
            appendMetadataToRepository(repositoryID, tmpAppendMetadata)

        val tmpurlInfo =
            jacksonObjectMapper().readValue(response, AppendResponse::class.java).appendFiles.associate {
                Pair(
//                it.name.split("/").last(),
                    it.name,
                    it.params.url,
                )
            }
        // FIX ME: Here we need to get the new folder structure as a response from the appendMetadataToRepository call
        // and use that to upload the files
//        val tmpMap = fileinfo.keys.map { "$tmpdirUUID/$it" to it }.toMap()

        val tmpMap = fileinfo.keys.map { "$it" to it }.toMap()

        val measureTime =
            measureTime {
                val MAX_CONCURRENT_UPLOADS = 10
                runBlocking {
                    val uploadContext = Dispatchers.IO + CoroutineName("UploadCoroutine")
                    val uploadSemaphore = Semaphore(MAX_CONCURRENT_UPLOADS)
                    val deferredUploads =
                        tmpMap.keys.map { key ->
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
                                            println("Failed to upload $filePath: ${ex.message}")
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

    suspend fun uploadBlob(
        sasUrl: String,
        uploadDir: String,
    ) {
        if (Files.notExists(Paths.get(uploadDir))) {
            logger.info("Folder does not exist")
            return
        }

        try {
//            logger.info("Uploading file: $uploadDir to $sasUrl")
            println("Uploading file: $uploadDir")
            var blobclient = BlobClientBuilder().endpoint(sasUrl).buildClient().uploadFromFile(uploadDir, true)
            logger.info("Finished Uploading $uploadDir with response $blobclient")
            println("Finished uploading file: $uploadDir")
        } catch (ex: UncheckedIOException) {
            logger.info("Failed to upload from file ${ex.message}")
            println("Failed to upload from file ${ex.message}")
        }
    }

    // Upload the files to the repository

    fun appendMetadataToRepository(
        repositoryID: String,
        content: Any,
    ): String {
        val contentTypeName = getContentTypeOfRepository(repositoryID)
        val contentTypePath = getPathofContentType(contentTypeName)
        val finalCookie = getCookie()

        val response =
            webClient.post()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        .path(
                            "/routers/Search/{encodedProjectID}/{encodedProjectType}" +
                                "/{encodedProjectTypeValue}/{contentTypePath}/artifacts/{repositoryID}/append",
                        )
                        .encode()
                        .buildAndExpand(
                            encodedProjectID,
                            encodedProjectType,
                            this.encodedProjectTypeValue,
                            contentTypePath,
                            repositoryID,
                        )
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
