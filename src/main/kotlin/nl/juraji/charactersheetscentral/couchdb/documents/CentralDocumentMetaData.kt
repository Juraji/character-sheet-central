package nl.juraji.charactersheetscentral.couchdb.documents

import com.fasterxml.jackson.annotation.JsonProperty

data class CentralDocumentMetaData(
    override val id: String?,
    override val rev: String?,
    override val modelType: String = "UNKNOWN",
    @JsonProperty("_attachments")
    val attachments: Map<String, CouchDbAttachmentStub> = emptyMap()
) : CentralDocument
