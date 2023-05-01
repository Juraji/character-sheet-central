package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument

data class CentralOAuthAuthorization(
    override val id: String? = null,
    override val rev: String? = null,
    val registeredClientId: String,
    val principalName: String,
    val authorizationGrantType: String,
    val authorizedScopes: Set<String>,
    val state: String?,
    val serializedAttributes: String,
    val serializedAuthorizationCode: String? = null,
    val serializedAccessToken: String? = null,
    val serializedOidcIdToken: String? = null,
    val serializedRefreshToken: String? = null,
) : CentralDocument
