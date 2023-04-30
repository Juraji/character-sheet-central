package nl.juraji.charactersheetscentral.services.oauth

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import nl.juraji.charactersheetscentral.util.jackson.GrantedAuthorityListDeserializer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient

data class CentralAuthorizationConsent(
    override val id: String? = null,
    override val rev: String? = null,
    val registeredClientId: String,
    val principalName: String,
    @JsonDeserialize(using = GrantedAuthorityListDeserializer::class)
    val authorities: Set<GrantedAuthority>,
) : CentralDocument

data class CentralAuthorizationConsentWithClient(
    val client: RegisteredClient,
    val consent: CentralAuthorizationConsent
)
