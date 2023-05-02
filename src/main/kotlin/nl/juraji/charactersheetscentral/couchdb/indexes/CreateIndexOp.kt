package nl.juraji.charactersheetscentral.couchdb.indexes

import com.fasterxml.jackson.annotation.JsonProperty
import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import nl.juraji.charactersheetscentral.couchdb.find.FindQuery

data class Index(
    val fields: Set<String>,
    @JsonProperty("partial_filter_selector")
    val partialFilterSelector: Map<String, Any>?
) {
    constructor(
        fields: Set<String>,
        partialFilterSelector: FindQuery<out CentralDocument>? = null
    ) : this(fields, partialFilterSelector?.selector)
}

data class CreateIndexOp(
    val name: String,
    val index: Index,
    @JsonProperty("ddoc")
    val designDocumentName: String = name,
    @JsonProperty("type")
    val indexType: String = "json",
    val partitioned: Boolean = false,
)
