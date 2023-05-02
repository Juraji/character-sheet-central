package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument

data class CentralUser(
    override val id: String? = null,
    override val rev: String? = null,
    val username: String,
    val password: String,
    val enabled: Boolean = true,
    val accountNonExpired: Boolean = false,
    val accountNonLocked: Boolean = false,
    val credentialsNonExpired: Boolean = false,
    val authorities: Set<String> = emptySet(),
) : CentralDocument
