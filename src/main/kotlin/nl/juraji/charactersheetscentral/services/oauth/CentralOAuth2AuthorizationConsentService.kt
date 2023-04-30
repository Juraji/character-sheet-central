package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class CentralOAuth2AuthorizationConsentService(
    private val registeredClientRepository: RegisteredClientRepository,
    configuration: CentralConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralAuthorizationConsent>(couchDb), OAuth2AuthorizationConsentService {
    override val databaseName: String = configuration.rootDbName
    override val documentClass: KClass<CentralAuthorizationConsent> = CentralAuthorizationConsent::class
    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralAuthorizationConsent>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralAuthorizationConsent>>() {}

    fun findAllByPrincipal(principalName: String): List<CentralAuthorizationConsentWithClient> = DocumentSelector
        .select("principalName" to principalName)
        .let(::findDocumentsBySelector)
        .associateWith { registeredClientRepository.findByClientId(it.registeredClientId) }
        .filterValues { it != null }
        .map { (consent, client) -> CentralAuthorizationConsentWithClient(client = client!!, consent = consent) }

    override fun findById(registeredClientId: String, principalName: String): OAuth2AuthorizationConsent? =
        idQuery(registeredClientId, principalName)
            .let(::findOneDocumentBySelector)
            ?.run {
                OAuth2AuthorizationConsent
                    .withId(registeredClientId, principalName)
                    .authorities { it.addAll(authorities) }
                    .build()
            }

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

    private fun idQuery(registeredClientId: String, principalName: String): DocumentSelector = DocumentSelector.select(
        "registeredClientId" to registeredClientId,
        "principalName" to principalName
    )
}
