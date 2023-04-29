package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.configuration.CouchCbConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindOperationResult
import nl.juraji.charactersheetscentral.couchcb.find.FindQuery
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class CentralOAuth2AuthorizationConsentService(
    private val configuration: CouchCbConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralAuthorizationConsent>(couchDb), OAuth2AuthorizationConsentService {

    override val databaseName: String
        get() = configuration.authorizationsDatabaseName

    override val documentClass: KClass<CentralAuthorizationConsent>
        get() = CentralAuthorizationConsent::class

    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindOperationResult<CentralAuthorizationConsent>> by lazy {
        object : ParameterizedTypeReference<ApiFindOperationResult<CentralAuthorizationConsent>>() {}
    }

    override fun findById(registeredClientId: String, principalName: String): OAuth2AuthorizationConsent? =
        idQuery(registeredClientId, principalName)
            .let(::findOneDocumentBySelector)
            ?.toOAuth2AuthorizationConsent()

    override fun save(authorizationConsent: OAuth2AuthorizationConsent) {
        authorizationConsent
            .run {
                idQuery(registeredClientId, principalName)
                    .let(::findOneDocumentBySelector)
                    ?.copy(authorities = authorities)
                    ?: CentralAuthorizationConsent(
                        registeredClientId = registeredClientId,
                        principalName = principalName,
                        authorities = authorities
                    )
            }
            .let(::saveDocument)
    }

    override fun remove(authorizationConsent: OAuth2AuthorizationConsent) {
        authorizationConsent
            .run { idQuery(registeredClientId, principalName) }
            .let(::findOneDocumentBySelector)
            ?.let(::deleteDocument)
    }

    private fun CentralAuthorizationConsent.toOAuth2AuthorizationConsent(): OAuth2AuthorizationConsent =
        OAuth2AuthorizationConsent
            .withId(registeredClientId, principalName)
            .authorities { it.addAll(authorities) }
            .build()

    private fun idQuery(registeredClientId: String, principalName: String): FindQuery = FindQuery(
        "registeredClientId" to registeredClientId,
        "principalName" to principalName
    )
}
