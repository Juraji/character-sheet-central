package nl.juraji.charactersheetscentral.services.inboxes

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument

data class OutboxMessage(
    override val id: String? = null,
    override val rev: String? = null,
    val receiver: String,
    val status: OutBoxSendStatus = OutBoxSendStatus.NEW,
    val message: String,
    val documentIds: Set<String>,
) : CentralDocument
