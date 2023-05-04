package nl.juraji.charactersheetscentral.couchdb.documents

import com.fasterxml.jackson.annotation.JsonProperty

data class CouchDbAttachmentStub(
    @JsonProperty("content_type")
    val contentType: String,
    val digest: String,
    val length: Long,
    @JsonProperty("revpos")
    val revisionNumber: Int,
    val stub: Boolean,

    // Not part of official CouchDb api, used for convenience
    val name: String? = null,
)
