package nl.juraji.charactersheetscentral.couchdb.find

import nl.juraji.charactersheetscentral.couchdb.documents.DocumentIdMeta

data class FindResult<T : DocumentIdMeta>(
    val docs: List<T>,
)
