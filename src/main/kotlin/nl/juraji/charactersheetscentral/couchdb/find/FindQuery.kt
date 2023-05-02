package nl.juraji.charactersheetscentral.couchdb.find

import com.fasterxml.jackson.annotation.JsonProperty
import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument

/**
 * See https://docs.couchdb.org/en/stable/api/database/find.html for more information
 */
data class FindQuery<T : CentralDocument>(
    val selector: Selector,
    val sort: List<Map<String, String>>? = null,
    val limit: Int = 25,
    val skip: Int = 0,
    val fields: Set<String>? = null,
    @JsonProperty("use_index")
    val useIndex: Set<String>? = null
)
