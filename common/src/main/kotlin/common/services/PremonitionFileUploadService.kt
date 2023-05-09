package common.services

import common.services.auth.AuthService
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Service
class PremonitionFileUploadService(
    private val webClient: WebClient,
    val authService: AuthService
) {

    fun uploadFile(resource: Resource, uploadURL: String): Mono<HttpStatus> {
        val token = authService.getAuthToken()
        val url =
            UriComponentsBuilder.fromHttpUrl(uploadURL).build(false).toUri()
        return webClient.post()
            .uri(url)
            .headers { it.setBearerAuth(token) }

//            .contentType()
            .body(BodyInserters.fromResource(resource))
            .exchangeToMono { response: ClientResponse ->
                if (response.statusCode() == HttpStatus.OK) {
                    return@exchangeToMono response.bodyToMono(HttpStatus::class.java)
                        .thenReturn(response.statusCode())
                } else {
                    throw Exception("Error Uploading Data")
                }
            }
    }

    fun uploadMultipart(multipartFile: MultipartFile, uploadURL: String): Mono<HttpStatus> {
        val token = authService.getAuthToken()

        val url =
            UriComponentsBuilder.fromHttpUrl(uploadURL).build(false).toUri()
        val builder = MultipartBodyBuilder()
        builder.part("file", multipartFile.resource)
        return webClient.post()
            .uri(url)
            .headers { it.setBearerAuth(token) }

            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .exchangeToMono { response: ClientResponse ->
                if (response.statusCode() == HttpStatus.OK) {
                    return@exchangeToMono response.bodyToMono(HttpStatus::class.java)
                        .thenReturn(response.statusCode())
                } else {
                    throw Exception("Error uploading file")
                }
            }
    }

}