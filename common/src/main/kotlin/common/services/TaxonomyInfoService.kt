package common.services

import common.model.ContentContent
import common.model.TaxonomyContentType
import common.services.auth.AuthService
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
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

    fun getTokens() {
        aadToken = authServiceObj.getAuthToken()
        webgmeAccesstoken = getWebGMEToken().toString()
    }

    fun getCookie(): String {
        return "webgme_aad=$aadToken; access_token=$webgmeAccesstoken ;"
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
                .onStatus(HttpStatus::is2xxSuccessful) {
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

            println("Exiting out of downloadFile")
            when(fileName)
            {
                "" -> fileName = UUID.randomUUID().toString()
            }
            val destination = Paths.get(dir, fileName)
            DataBufferUtils.write(response, destination).block()
            var renamedPath = Paths.get(dir, fileName)

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
                if (it.first in requestedContentType || requestedContentType.isEmpty()) {
                    async {
                        val artifactResponse = getContentRecordRequest(it.second, finalCookie)
//                        println("Artifact Response: $artifactResponse")
                        it.first to artifactResponse
                    }
                } else {
                    null
                }
            }
            val tmp = results?.awaitAll() ?: emptyList()
            tmp.forEach { (contentType, content) ->
                content?.let { combinedresult?.set(contentType, it) }
            }
        }
        return combinedresult
    }

    private suspend fun getContentRecordRequest(contentURI: String, finalCookie: String): ContentContent? {

//        https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Search/AllLeap%2BTaxonomyBootcamp/branch/master/%2FH/artifacts/
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
                .awaitSingle()
        return artifactResponse
    }

    fun getDownloadURL(processID: String, index: String): String {
//        val encodedProjectID = "AllLeap+TaxonomyBootcamp"
//        val encodedProjectBranch = "master"

        val contentTypeName = getContentTypeOfRepository(processID)
        val contentTypePath = getPathofContentType(contentTypeName)
        println("Content Type Path: $contentTypePath" + "Content Type Name: $contentTypeName")


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
}