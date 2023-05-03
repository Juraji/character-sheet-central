package nl.juraji.charactersheetscentral.services.inboxes

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument

data class OutboxDocument(
    override val id: String? = null,
    override val rev: String? = null,
    val receivers: Set<String>,
    val message: String,
    val documents: Set<String>,
    val attachments: Set<Any>,
) : CentralDocument
