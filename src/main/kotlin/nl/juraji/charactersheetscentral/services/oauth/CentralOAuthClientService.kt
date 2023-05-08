package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.couchdb.DocumentRepository
import nl.juraji.charactersheetscentral.couchdb.documents.SaveType
import nl.juraji.charactersheetscentral.couchdb.find.FindResult
import nl.juraji.charactersheetscentral.couchdb.find.eq
import nl.juraji.charactersheetscentral.couchdb.find.modelQuery
import nl.juraji.charactersheetscentral.couchdb.find.query
import nl.juraji.charactersheetscentral.couchdb.indexes.CreateIndexOp
import nl.juraji.charactersheetscentral.couchdb.indexes.Index
import nl.juraji.charactersheetscentral.couchdb.indexes.partialFilterSelector
import nl.juraji.charactersheetscentral.util.jackson.restTemplateTypeRef
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Repository
import java.time.Duration
import kotlin.reflect.KClass

@Repository
class CentralOAuthClientService(
    configuration: CentralConfiguration,
    couchDb: CouchDbService
) : DocumentRepository<CentralOAuthClient>(couchDb), RegisteredClientRepository {
    override val databaseName: String = configuration.rootDbName
    override val documentClass: KClass<CentralOAuthClient> = CentralOAuthClient::class
    override val documentFindTypeRef: ParameterizedTypeReference<FindResult<CentralOAuthClient>>
        get() = restTemplateTypeRef()

    fun findAll(): List<CentralOAuthClient> = findDocumentsBySelector(query())

    override fun findById(id: String): RegisteredClient? =
        findDocumentById(id)?.asRegisteredClient()

    override fun findByClientId(clientId: String): RegisteredClient? =
        findOneDocumentBySelector(modelQuery(eq("clientId", clientId)))?.asRegisteredClient()

    override fun save(registeredClient: RegisteredClient) {
        val existing = findDocumentById(registeredClient.id)
        val update = registeredClient.asCentralOAuthClient()

        if (existing !== null) saveDocument(update.copy(id = existing.id, rev = existing.rev))
        else saveDocument(update, action = SaveType.CREATE)
    }

    override fun defineIndexes(): List<CreateIndexOp> {
        val selector = partialFilterSelector(CentralOAuthClient::class)

        return listOf(
            CreateIndexOp(
                name = IDX_CLIENT_ID,
                index = Index(
                    fields = setOf("clientId"),
                    partialFilterSelector = selector
                )
            ),
        )
    }

    fun defaultClient(): CentralOAuthClient = CentralOAuthClient(
        clientName = "",
        clientId = "",
        clientSecret = "",
        redirectUris = emptySet(),
        clientAuthenticationMethods = setOf(
            ClientAuthenticationMethod.CLIENT_SECRET_BASIC.value,
            ClientAuthenticationMethod.CLIENT_SECRET_POST.value,
        ),
        authorizationGrantTypes = setOf(
            AuthorizationGrantType.AUTHORIZATION_CODE.value,
            AuthorizationGrantType.REFRESH_TOKEN.value,
            AuthorizationGrantType.CLIENT_CREDENTIALS.value,
        ),
        scopes = setOf(
            OidcScopes.OPENID,
            OidcScopes.PROFILE,
            CentralScopes.COUCHDB_SYNC
        ),
        accessTokenTtl = Duration.ZERO,
        refreshTokenTtl = Duration.ZERO,
        requireAuthorizationConsent = true,
    )

    private fun CentralOAuthClient.asRegisteredClient(): RegisteredClient {
        return RegisteredClient
            .withId(id)
            .clientId(clientId)
            .clientName(clientName)
            .clientSecret(clientSecret)
            .apply {
                redirectUris.forEach(::redirectUri)
                clientAuthenticationMethods.map(::ClientAuthenticationMethod).forEach(::clientAuthenticationMethod)
                authorizationGrantTypes.map(::AuthorizationGrantType).forEach(::authorizationGrantType)
                scopes.forEach(::scope)
            }
            .clientSettings(
                ClientSettings.builder()
                    .requireAuthorizationConsent(requireAuthorizationConsent)
                    .build()
            )
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(accessTokenTtl)
                    .refreshTokenTimeToLive(refreshTokenTtl)
                    .build()
            )
            .build()
    }

    private fun RegisteredClient.asCentralOAuthClient(): CentralOAuthClient = CentralOAuthClient(
        clientName = clientName,
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUris = redirectUris,
        clientAuthenticationMethods = clientAuthenticationMethods
            .map(ClientAuthenticationMethod::getValue)
            .toSet(),
        authorizationGrantTypes = authorizationGrantTypes
            .map(AuthorizationGrantType::getValue)
            .toSet(),
        scopes = scopes,
        accessTokenTtl = tokenSettings.accessTokenTimeToLive,
        refreshTokenTtl = tokenSettings.refreshTokenTimeToLive,
        requireAuthorizationConsent = clientSettings.isRequireAuthorizationConsent,
    )

    companion object {
        const val IDX_CLIENT_ID = "idx__centralOAuthClient__clientId"
    }
}
