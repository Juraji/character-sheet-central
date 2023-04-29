package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.couchcb.support.DocumentIdMeta
import org.springframework.security.core.GrantedAuthority

data class CentralUser(
    override val id: String?,
    override val rev: String?,
    val username: String,
    val password: String,
    val passwordSalt: String,
    val disabled: Boolean = false,
    val accountExpired: Boolean = false,
    val accountLocked: Boolean = false,
    val credentialsExpired: Boolean = false,
    val authorities: List<GrantedAuthority> = emptyList(),
) : DocumentIdMeta
