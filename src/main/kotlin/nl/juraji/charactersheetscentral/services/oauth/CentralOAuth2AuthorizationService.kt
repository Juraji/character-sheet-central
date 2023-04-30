package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.util.assertNotNull
import org.springframework.core.ParameterizedTypeReference
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
        val query = when (tokenType?.value) {
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

        val update = existing
            ?.copy(
                attributes = attributes,
                state = state,
                authorizationCode = authorizationCode,
                accessToken = accessToken,
                oidcIdToken = oidcIdToken,
                refreshToken = refreshToken,
            )
            ?: CentralOAuthAuthorization(
                id = authorization.id,
                registeredClientId = authorization.registeredClientId,
                principalName = authorization.principalName,
                authorizationGrantType = authorization.authorizationGrantType,
                authorizedScopes = authorization.authorizedScopes,
                attributes = attributes,
                state = state,
                authorizationCode = authorizationCode,
                accessToken = accessToken,
                oidcIdToken = oidcIdToken,
                refreshToken = refreshToken,
            )

        saveDocument(update)
    }

    override fun remove(authorization: OAuth2Authorization) {
        findDocumentById(authorization.id)?.let(::deleteDocument)
    }

    private fun CentralOAuthAuthorization.toOAuth2Authorization(): OAuth2Authorization {
        val registeredClient = registeredClientRepository.findById(registeredClientId)

        val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
            .id(id)
            .principalName(principalName)
            .authorizationGrantType(authorizationGrantType)
            .authorizedScopes(authorizedScopes)
            .attributes { it.putAll(attributes) }

        state?.let { builder.attribute(OAuth2ParameterNames.STATE, it) }
        authorizationCode?.let { builder.token(it) }
        accessToken?.let { builder.token(it) }
        oidcIdToken?.let { builder.token(it) }
        refreshToken?.let { builder.token(it) }

        return builder.build()
    }
}
