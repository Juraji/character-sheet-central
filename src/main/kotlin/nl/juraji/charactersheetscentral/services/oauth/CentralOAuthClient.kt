package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import java.time.Duration

data class CentralOAuthClient(
    override val id: String? = null,
    override val rev: String? = null,
    val clientName: String,
    val clientId: String,
    val clientSecret: String?,
    val redirectUris: Set<String>,
    val clientAuthenticationMethods: Set<String>,
    val authorizationGrantTypes: Set<String>,
    val scopes: Set<String>,
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration,
    val requireAuthorizationConsent: Boolean,
) : CentralDocument
