package nl.juraji.charactersheetscentral.couchcb.support

data class CouchDbUserDocument(
    override val id: String? = null,
    override val rev: String? = null,
    val name: String,
    val password: String? = null,
    val roles: Set<String> = emptySet(),
    val type: String = "user"
) : CentralDocument
