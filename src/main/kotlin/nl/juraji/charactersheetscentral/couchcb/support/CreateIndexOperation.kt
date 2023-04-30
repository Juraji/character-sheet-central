package nl.juraji.charactersheetscentral.couchcb.support

import com.fasterxml.jackson.annotation.JsonProperty
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector

data class Index(
    val fields: Set<String>,
    @JsonProperty("partial_filter_selector")
    val partialFilterSelector: DocumentSelector<CentralDocumentMetaData>?
)

data class CreateIndexOperation(
    @JsonProperty("name")
    val indexName: String,
    val index: Index,
    @JsonProperty("ddoc")
    val designDocumentName: String? = null,
    @JsonProperty("type")
    val indexType: String = "json",
    val partitioned: Boolean = false,
)
