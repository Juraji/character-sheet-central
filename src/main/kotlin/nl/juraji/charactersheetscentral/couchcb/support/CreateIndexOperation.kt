package nl.juraji.charactersheetscentral.couchcb.support

import com.fasterxml.jackson.annotation.JsonProperty
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector

data class Index(
    val fields: Set<String>,
    @JsonProperty("partial_filter_selector")
    val partialFilterSelector: Map<String, Any>?
) {
    constructor(
        fields: Set<String>,
        partialFilterSelector: DocumentSelector<out CentralDocument>? = null
    ) : this(fields, partialFilterSelector?.selector)
}

data class CreateIndexOperation(
    val name: String,
    val index: Index,
    @JsonProperty("ddoc")
    val designDocumentName: String = name,
    @JsonProperty("type")
    val indexType: String = "json",
    val partitioned: Boolean = false,
)
