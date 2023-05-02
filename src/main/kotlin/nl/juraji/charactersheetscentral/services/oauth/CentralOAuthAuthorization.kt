package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import java.time.Instant

data class CentralOAuthAuthorization(
    override val id: String? = null,
    override val rev: String? = null,
    val registeredClientId: String,
    val principalName: String,
    val authorizationGrantType: String,
    val authorizedScopes: Set<String>,
    val state: String?,
    val attributes: String,

    // Code
    val authorizationCode: String? = null,
    val authorizationCodeIssuedAt: Instant? = null,
    val authorizationCodeExpiresAt: Instant? = null,
    val authorizationCodeMetadata: String? = null,

    // Access token
    val accessToken: String? = null,
    val accessTokenIssuedAt: Instant? = null,
    val accessTokenExpiresAt: Instant? = null,
    val accessTokenScopes: Set<String>? = null,
    val accessTokenMetadata: String? = null,

    // OIDC
    val oidcIdToken: String? = null,
    val oidcIdTokenIssuedAt: Instant? = null,
    val oidcIdTokenExpiresAt: Instant? = null,
    val oidcIdTokenMetadata: String? = null,

    // Refresh token
    val refreshToken: String? = null,
    val refreshTokenIssuedAt: Instant? = null,
    val refreshTokenExpiresAt: Instant? = null,
    val refreshTokenMetadata: String? = null,
) : CentralDocument
