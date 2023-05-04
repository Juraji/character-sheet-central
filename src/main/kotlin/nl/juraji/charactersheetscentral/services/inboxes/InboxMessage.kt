package nl.juraji.charactersheetscentral.services.inboxes

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import nl.juraji.charactersheetscentral.couchdb.documents.CouchDbAttachmentStub

data class InboxDocument(
    override val id: String,
    override val rev: String,
    override val modelType: String,
    val documentData: String,
    val attachmentStubs: Map<String, CouchDbAttachmentStub>,
) : CentralDocument

data class InboxMessage(
    override val id: String? = null,
    override val rev: String? = null,
    val sender: String,
    val message: String,
    val documents: List<InboxDocument>
) : CentralDocument

