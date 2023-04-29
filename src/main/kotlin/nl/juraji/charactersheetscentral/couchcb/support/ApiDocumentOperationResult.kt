package nl.juraji.charactersheetscentral.couchcb.support

data class ApiDocumentOperationResult(
    // Document ID
    val id: String,
    // Revision MVCC token
    val rev: String,
)
