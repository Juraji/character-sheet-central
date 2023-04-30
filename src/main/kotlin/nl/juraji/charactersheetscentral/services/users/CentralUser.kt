package nl.juraji.charactersheetscentral.services.users

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import nl.juraji.charactersheetscentral.util.jackson.GrantedAuthorityListDeserializer
import org.springframework.security.core.GrantedAuthority

data class CentralUser(
    override val id: String? = null,
    override val rev: String? = null,
    val username: String,
    val password: String,
    val enabled: Boolean = true,
    val accountNonExpired: Boolean = false,
    val accountNonLocked: Boolean = false,
    val credentialsNonExpired: Boolean = false,
    @JsonDeserialize(using = GrantedAuthorityListDeserializer::class)
    val authorities: List<GrantedAuthority> = emptyList(),
) : CentralDocument
