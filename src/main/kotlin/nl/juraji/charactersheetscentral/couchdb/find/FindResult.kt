package nl.juraji.charactersheetscentral.couchdb.find

import nl.juraji.charactersheetscentral.couchdb.documents.CouchDbDocument

data class FindResult<T : CouchDbDocument>(
    val docs: List<T>,
)
