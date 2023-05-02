package nl.juraji.charactersheetscentral.couchdb.support

data class CouchOperationError(
    val error: String? = null,
    val reason: String? = null,
)
