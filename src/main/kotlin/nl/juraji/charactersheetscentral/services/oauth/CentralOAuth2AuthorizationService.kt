package nl.juraji.charactersheetscentral.services.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.CreateIndexOperation
import nl.juraji.charactersheetscentral.couchcb.support.Index
import nl.juraji.charactersheetscentral.couchcb.support.SaveAction
import nl.juraji.charactersheetscentral.util.assertNotNull
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.jackson2.CoreJackson2Module
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.web.jackson2.WebJackson2Module
import org.springframework.security.web.jackson2.WebServletJackson2Module
import org.springframework.security.web.server.jackson2.WebServerJackson2Module
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class CentralOAuth2AuthorizationService(
    private val registeredClientRepository: RegisteredClientRepository,
    private val objectMapperBuilder: Jackson2ObjectMapperBuilder,
    configuration: CentralConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralOAuthAuthorization>(couchDb), OAuth2AuthorizationService {

    override val databaseName: String = configuration.rootDbName

    override val documentClass: KClass<CentralOAuthAuthorization> = CentralOAuthAuthorization::class

    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralOAuthAuthorization>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralOAuthAuthorization>>() {}

    private val authorizationObjectMapper: ObjectMapper by lazy {
        objectMapperBuilder
            .build<ObjectMapper>()
            .registerModules(
                CoreJackson2Module(),
                WebJackson2Module(),
                WebServerJackson2Module(),
                WebServletJackson2Module(),
                JavaTimeModule(),
                OAuth2AuthorizationServerJackson2Module(),
            )
    }

    override fun findById(id: String): OAuth2Authorization? =
        findDocumentById(id)?.toOAuth2Authorization()

    override fun findByToken(token: String, tokenType: OAuth2TokenType?): OAuth2Authorization? {
        val query: DocumentSelector<CentralOAuthAuthorization> = when (tokenType?.value) {
            OAuth2ParameterNames.STATE -> DocumentSelector
                .select<CentralOAuthAuthorization>("state" to token)
                .withIndex(IDX_STATE)

            OAuth2ParameterNames.CODE -> DocumentSelector
                .select<CentralOAuthAuthorization>("authorizationCode" to token)
                .withIndex(IDX_AUTHORIZATION_CODE)

            OAuth2ParameterNames.ACCESS_TOKEN -> DocumentSelector
                .select<CentralOAuthAuthorization>("accessToken" to token)
                .withIndex(IDX_ACCESS_TOKEN)

            OAuth2ParameterNames.REFRESH_TOKEN -> DocumentSelector
                .select<CentralOAuthAuthorization>("refreshToken" to token)
                .withIndex(IDX_REFRESH_TOKEN)

            else -> DocumentSelector
                .select<CentralOAuthAuthorization>(
                    "state" to token,
                    "authorizationCode" to token,
                    "accessToken" to token,
                    "refreshToken" to token,
                )
                .withIndex(IDX_ALL_TOKENS)
        }

        return findOneDocumentBySelector(query)?.toOAuth2Authorization()
    }

    override fun save(authorization: OAuth2Authorization) {
        assertNotNull(authorization.id)

        val ser = authorizationObjectMapper::writeValueAsString

        val authorizedScopes = authorization.authorizedScopes
        val state = authorization.getAttribute<String>(OAuth2ParameterNames.STATE)?.takeIf(String::isNotBlank)
        val attributes = ser(authorization.attributes)

        val existing = findDocumentById(authorization.id)
        var update: CentralOAuthAuthorization = existing
            ?.copy(
                authorizedScopes = authorizedScopes,
                state = state,
                attributes = attributes,
            )
            ?: CentralOAuthAuthorization(
                id = authorization.id,
                registeredClientId = authorization.registeredClientId,
                principalName = authorization.principalName,
                authorizationGrantType = authorization.authorizationGrantType.value,
                authorizedScopes = authorizedScopes,
                state = state,
                attributes = attributes,
            )

        authorization.getToken(OAuth2AuthorizationCode::class.java)?.let {
            update = update.copy(
                authorizationCode = it.token.tokenValue,
                authorizationCodeIssuedAt = it.token.issuedAt,
                authorizationCodeExpiresAt = it.token.expiresAt,
                authorizationCodeMetadata = ser(it.metadata),
            )
        }

        authorization.getToken(OAuth2AccessToken::class.java)?.let {
            update = update.copy(
                accessToken = it.token.tokenValue,
                accessTokenIssuedAt = it.token.issuedAt,
                accessTokenExpiresAt = it.token.expiresAt,
                accessTokenScopes = it.token.scopes,
                accessTokenMetadata = ser(it.metadata),
            )
        }

        authorization.getToken(OidcIdToken::class.java)?.let {
            update = update.copy(
                oidcIdToken = it.token.tokenValue,
                oidcIdTokenIssuedAt = it.token.issuedAt,
                oidcIdTokenExpiresAt = it.token.expiresAt,
                oidcIdTokenMetadata = ser(it.metadata),
            )
        }

        authorization.getToken(OAuth2RefreshToken::class.java)?.let {
            update = update.copy(
                refreshToken = it.token.tokenValue,
                refreshTokenIssuedAt = it.token.issuedAt,
                refreshTokenExpiresAt = it.token.expiresAt,
                refreshTokenMetadata = ser(it.metadata),
            )
        }

        when (existing) {
            null -> saveDocument(update, SaveAction.CREATE)
            else -> saveDocument(update, SaveAction.UPDATE)
        }

    }

    override fun remove(authorization: OAuth2Authorization) {
        findDocumentById(authorization.id)?.let(::deleteDocument)
    }

    override fun defineIndexes(): List<CreateIndexOperation> {
        val selector = DocumentSelector.partialFilterSelector(CentralOAuthAuthorization::class)
        fun selectNonNull(field: String): Pair<String, Any> = field to mapOf(DocumentSelector.Match.TYPE to "string")

        return listOf(
            CreateIndexOperation(
                name = IDX_STATE,
                index = Index(
                    fields = setOf("state"),
                    partialFilterSelector = selector
                        .appendSelectors(selectNonNull("state"))
                )
            ),
            CreateIndexOperation(
                name = IDX_AUTHORIZATION_CODE,
                index = Index(
                    fields = setOf("authorizationCode"),
                    partialFilterSelector = selector
                        .appendSelectors(selectNonNull("authorizationCode"))
                )
            ),
            CreateIndexOperation(
                name = IDX_ACCESS_TOKEN,
                index = Index(
                    fields = setOf("accessToken"),
                    partialFilterSelector = selector
                        .appendSelectors(selectNonNull("accessToken"))
                )
            ),
            CreateIndexOperation(
                name = IDX_REFRESH_TOKEN,
                index = Index(
                    fields = setOf("refreshToken"),
                    partialFilterSelector = selector
                        .appendSelectors(selectNonNull("refreshToken"))
                )
            ),
            CreateIndexOperation(
                name = IDX_ALL_TOKENS,
                index = Index(
                    fields = setOf("state", "authorizationCode", "accessToken", "refreshToken"),
                    partialFilterSelector = selector
                )
            ),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun CentralOAuthAuthorization.toOAuth2Authorization(): OAuth2Authorization {
        val registeredClient = registeredClientRepository.findById(registeredClientId)
        val mapTypeRef = jacksonTypeRef<Map<String, Any>>()

        fun readMeta(it: String?): Map<String, Any> =
            if (it == null) emptyMap()
            else authorizationObjectMapper.readValue(it, mapTypeRef)

        val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
            .id(id)
            .principalName(principalName)
            .authorizationGrantType(AuthorizationGrantType(authorizationGrantType))
            .authorizedScopes(authorizedScopes)

        state?.let { builder.attribute(OAuth2ParameterNames.STATE, it) }
        attributes
            .let { authorizationObjectMapper.readValue(it, mapTypeRef) }
            .let { attrs -> builder.attributes { it.putAll(attrs) } }

        if (authorizationCode != null) {
            val metaData = readMeta(authorizationCodeMetadata)

            val token = OAuth2AuthorizationCode(
                authorizationCode,
                authorizationCodeIssuedAt,
                authorizationCodeExpiresAt
            )

            builder.token(token) { it.putAll(metaData) }
        }

        if (accessToken != null) {
            val metaData = readMeta(accessTokenMetadata)

            val token = OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessToken,
                accessTokenIssuedAt,
                accessTokenExpiresAt
            )

            builder.token(token) { it.putAll(metaData) }
        }

        if (oidcIdToken != null) {
            val metaData = readMeta(oidcIdTokenMetadata)
            val claims = metaData[OAuth2Authorization.Token.CLAIMS_METADATA_NAME] as Map<String, Any>

            val token = OidcIdToken(
                oidcIdToken,
                oidcIdTokenIssuedAt,
                oidcIdTokenExpiresAt,
                claims
            )

            builder.token(token) { it.putAll(metaData) }
        }

        if (refreshToken != null) {
            val metaData = readMeta(refreshTokenMetadata)

            val token = OAuth2RefreshToken(
                refreshToken,
                refreshTokenIssuedAt,
                refreshTokenExpiresAt
            )

            builder.token(token) { it.putAll(metaData) }
        }

        return builder.build()
    }

    companion object {
        const val IDX_STATE = "idx__centralOAuthAuthorization__state"
        const val IDX_AUTHORIZATION_CODE = "idx__centralOAuthAuthorization__authorizationCode"
        const val IDX_ACCESS_TOKEN = "idx__centralOAuthAuthorization__accessToken"
        const val IDX_REFRESH_TOKEN = "idx__centralOAuthAuthorization__refreshToken"
        const val IDX_ALL_TOKENS = "idx__centralOAuthAuthorization__allTokens"
    }
}
