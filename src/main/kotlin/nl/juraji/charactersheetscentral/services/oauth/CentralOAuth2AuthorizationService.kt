package nl.juraji.charactersheetscentral.services.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.SaveAction
import nl.juraji.charactersheetscentral.util.assertNotNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
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
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class CentralOAuth2AuthorizationService(
    private val registeredClientRepository: RegisteredClientRepository,
    @Qualifier("oauth2objectMapper") private val oauth2ObjectMapper: ObjectMapper,
    configuration: CentralConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralOAuthAuthorization>(couchDb), OAuth2AuthorizationService {

    override val databaseName: String = configuration.rootDbName

    override val documentClass: KClass<CentralOAuthAuthorization> = CentralOAuthAuthorization::class

    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralOAuthAuthorization>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralOAuthAuthorization>>() {}

    override fun findById(id: String): OAuth2Authorization? =
        findDocumentById(id)?.toOAuth2Authorization()

    override fun findByToken(token: String, tokenType: OAuth2TokenType?): OAuth2Authorization? {
        val tokenValueSelect = mapOf("tokenValue" to token)
        val query: DocumentSelector<CentralOAuthAuthorization> = when (tokenType?.value) {
            OAuth2ParameterNames.STATE -> DocumentSelector.select("state" to token)

            OAuth2ParameterNames.CODE -> DocumentSelector.select("authorizationCode" to tokenValueSelect)

            OAuth2ParameterNames.ACCESS_TOKEN -> DocumentSelector.select("accessToken" to tokenValueSelect)

            OAuth2ParameterNames.REFRESH_TOKEN -> DocumentSelector.select("refreshToken" to tokenValueSelect)

            else -> DocumentSelector.select(
                "authorizationCode" to tokenValueSelect,
                "accessToken" to tokenValueSelect,
                "refreshToken" to tokenValueSelect,
            )
        }

        return findOneDocumentBySelector(query)?.toOAuth2Authorization()
    }

    override fun save(authorization: OAuth2Authorization) {
        assertNotNull(authorization.id)

        val existing = findDocumentById(authorization.id)
        val attributes = authorization.attributes
        val state = authorization.getAttribute<String>(OAuth2ParameterNames.STATE)?.takeIf(String::isNotBlank)
        val authorizationCode = authorization.getToken(OAuth2AuthorizationCode::class.java)?.token
        val accessToken = authorization.getToken(OAuth2AccessToken::class.java)?.token
        val oidcIdToken = authorization.getToken(OidcIdToken::class.java)?.token
        val refreshToken = authorization.getToken(OAuth2RefreshToken::class.java)?.token

        if (existing != null) {
            existing
                .copy(
                    state = state,
                    serializedAttributes = oauth2ObjectMapper.writeValueAsString(attributes),
                    serializedAuthorizationCode = oauth2ObjectMapper.writeValueAsString(authorizationCode),
                    serializedAccessToken = oauth2ObjectMapper.writeValueAsString(accessToken),
                    serializedOidcIdToken = oauth2ObjectMapper.writeValueAsString(oidcIdToken),
                    serializedRefreshToken = oauth2ObjectMapper.writeValueAsString(refreshToken),
                )
                .let { saveDocument(it, SaveAction.UPDATE) }
        } else {
            CentralOAuthAuthorization(
                id = authorization.id,
                registeredClientId = authorization.registeredClientId,
                principalName = authorization.principalName,
                authorizationGrantType = authorization.authorizationGrantType.value,
                authorizedScopes = authorization.authorizedScopes,
                state = state,
                serializedAttributes = oauth2ObjectMapper.writeValueAsString(attributes),
                serializedAuthorizationCode = oauth2ObjectMapper.writeValueAsString(authorizationCode),
                serializedAccessToken = oauth2ObjectMapper.writeValueAsString(accessToken),
                serializedOidcIdToken = oauth2ObjectMapper.writeValueAsString(oidcIdToken),
                serializedRefreshToken = oauth2ObjectMapper.writeValueAsString(refreshToken),
            ).let { saveDocument(it, SaveAction.CREATE) }
        }
    }

    override fun remove(authorization: OAuth2Authorization) {
        findDocumentById(authorization.id)?.let(::deleteDocument)
    }

    private fun CentralOAuthAuthorization.toOAuth2Authorization(): OAuth2Authorization {
        val registeredClient = registeredClientRepository.findById(registeredClientId)

        val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
            .id(id)
            .principalName(principalName)
            .authorizationGrantType(AuthorizationGrantType(authorizationGrantType))
            .authorizedScopes(authorizedScopes)

        state?.let { builder.attribute(OAuth2ParameterNames.STATE, it) }

        serializedAttributes
            .let { oauth2ObjectMapper.readValue<Map<String, Any>>(it) }
            .let { attrs -> builder.attributes { it.putAll(attrs) } }
        serializedAuthorizationCode
            ?.let { oauth2ObjectMapper.readValue(it, OAuth2AuthorizationCode::class.java) }
            ?.let { builder.token(it) }
        serializedAccessToken
            ?.let { oauth2ObjectMapper.readValue(it, OAuth2AccessToken::class.java) }
            ?.let { builder.token(it) }
        serializedOidcIdToken
            ?.let { oauth2ObjectMapper.readValue(it, OidcIdToken::class.java) }
            ?.let { builder.token(it) }
        serializedRefreshToken
            ?.let { oauth2ObjectMapper.readValue(it, OAuth2RefreshToken::class.java) }
            ?.let { builder.token(it) }

        return builder.build()
    }
}
