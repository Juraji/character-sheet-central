package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode

data class CentralOAuthAuthorization(
    override val id: String? = null,
    override val rev: String? = null,
    val registeredClientId: String,
    val principalName: String,
    val authorizationGrantType: AuthorizationGrantType,
    val authorizedScopes: Set<String>,
    val attributes: Map<String, Any>,
    val state: String?,
    val authorizationCode: OAuth2AuthorizationCode? = null,
    val accessToken: OAuth2AccessToken? = null,
    val oidcIdToken: OidcIdToken? = null,
    val refreshToken: OAuth2RefreshToken? = null,
) : CentralDocument
