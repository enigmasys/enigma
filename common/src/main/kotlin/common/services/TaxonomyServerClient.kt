package common.services

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import reactor.netty.http.client.HttpClient

@Service
class TaxonomyServerClient {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun getGuidTags(
        url: String,
        projectID: String,
        projectBranch: String,
        humanTags: String,
    ): String? {
        val humanTagsEncoded = humanTags // java.net.URLEncoder.encode(humanTags, "utf-8")
        logger.info("Encoded tags: $humanTagsEncoded")
        logger.info("URL: $url")
        val encodedProjectID = projectID // java.net.URLEncoder.encode(projectID,"utf-8")
        val encodedProjectBranch = projectBranch // java.net.URLEncoder.encode(projectBranch,"utf-8")

        var response =
            WebClient.builder()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true),
                    ),
                )
                .baseUrl(url)
                .build()
                .get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
                        .path("routers/TagFormat/{encodedProjectID}/branch/{encodedProjectBranch}/guid")
                        .queryParam("tags", "{humanTagsEncoded}")
                        .encode()
                        .buildAndExpand(encodedProjectID, encodedProjectBranch, humanTagsEncoded)
                        .toUri()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java).block()
        return response
    }

    fun getHumanTags(
        url: String,
        projectID: String,
        projectBranch: String,
        guidTags: String,
    ): String? {
        val guidTagsEncoded = guidTags // java.net.URLEncoder.encode(humanTags, "utf-8")
        logger.info("Encoded tags: $guidTagsEncoded")
        logger.info("URL: $url")
        val encodedProjectID = projectID // java.net.URLEncoder.encode(projectID,"utf-8")
        val encodedProjectBranch = projectBranch // java.net.URLEncoder.encode(projectBranch,"utf-8")

        var response =
            WebClient.builder()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true),
                    ),
                )
                .baseUrl(url)
                .build()
                .get()
                .uri { uriBuilder: UriBuilder ->
                    UriComponentsBuilder.fromUri(uriBuilder.build())
//                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
                        .path("routers/TagFormat/{encodedProjectID}/branch/{encodedProjectBranch}/human")
                        .queryParam("tags", "{guidTagsEncoded}")
                        .encode()
                        .buildAndExpand(encodedProjectID, encodedProjectBranch, guidTagsEncoded)
//                .buildAndExpand(guidTagsEncoded)
                        .toUri()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java).block()
        return response
    }
}
