package nl.juraji.charactersheetscentral.couchdb.documents

import com.fasterxml.jackson.annotation.JsonProperty

interface CouchDbDocument {
    @get:JsonProperty("_id")
    val id: String?

    @get:JsonProperty("_rev")
    val rev: String?

    // For interface destructuring
    operator fun component1(): String? // id
    operator fun component2(): String? // rev
}
