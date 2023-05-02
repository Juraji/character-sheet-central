package nl.juraji.charactersheetscentral.couchdb.documents

data class DocumentOpResult(
    // Document ID
    val id: String,
    // Revision MVCC token
    val rev: String,
)
