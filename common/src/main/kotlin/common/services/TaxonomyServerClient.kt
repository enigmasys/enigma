package common.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder

@Service
class TaxonomyServerClient {

val logger = LoggerFactory.getLogger(this::class.java)

    fun getGuidTags(url: String, humanTags: String): String? {

        val humanTagsEncoded = java.net.URLEncoder.encode(humanTags, "utf-8")
        logger.info("Encoded tags: $humanTagsEncoded")
        var response = WebClient.builder()
            .baseUrl(url)
            .build()
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .queryParam("tags", humanTagsEncoded)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java).block()
        return response
    }
}