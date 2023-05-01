package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient

data class CentralAuthorizationConsent(
    override val id: String? = null,
    override val rev: String? = null,
    val registeredClientId: String,
    val principalName: String,
    val authorities: Set<GrantedAuthority>,
) : CentralDocument

data class CentralAuthorizationConsentWithClient(
    val client: RegisteredClient,
    val consent: CentralAuthorizationConsent
)
