package nl.juraji.charactersheetscentral.couchdb.users

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument

data class DbUserDocument(
    override val id: String? = null,
    override val rev: String? = null,
    val name: String,
    val password: String? = null,
    val roles: Set<String> = emptySet(),
    val type: String = "user"
) : CentralDocument
