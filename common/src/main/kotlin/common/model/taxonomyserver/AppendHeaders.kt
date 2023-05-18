package common.model.taxonomyserver


import com.fasterxml.jackson.annotation.JsonProperty

data class AppendHeaders(
    @JsonProperty("Accept")
    val accept: String,
    @JsonProperty("Content-Type")
    val contentType: String,
    @JsonProperty("x-ms-blob-type")
    val xMsBlobType: String,
    @JsonProperty("x-ms-encryption-algorithm")
    val xMsEncryptionAlgorithm: String
)