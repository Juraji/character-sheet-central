package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.CreateIndexOperation
import nl.juraji.charactersheetscentral.couchcb.support.Index
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
        .select<CentralAuthorizationConsent>("principalName" to principalName)
        .withIndex(PRINCIPAL_IDX)
        .let(::findDocumentsBySelector)
        .associateWith { registeredClientRepository.findByClientId(it.registeredClientId) }
        .filterValues { it != null }
        .map { (consent, client) -> CentralAuthorizationConsentWithClient(client = client!!, consent = consent) }

    override fun findById(registeredClientId: String, principalName: String): OAuth2AuthorizationConsent? =
        idQuery(registeredClientId, principalName)
            .let(::findOneDocumentBySelector)
            ?.run {
                val grantedAuthorities = authorities.map(::SimpleGrantedAuthority)

                OAuth2AuthorizationConsent
                    .withId(registeredClientId, principalName)
                    .authorities { it.addAll(grantedAuthorities) }
                    .build()
            }

    override fun save(authorizationConsent: OAuth2AuthorizationConsent) {
        authorizationConsent
            .run {
                val authoritiesSet = authorities.map { it.authority }.toSet()

                idQuery(registeredClientId, principalName)
                    .let(::findOneDocumentBySelector)
                    ?.copy(authorities = authoritiesSet)
                    ?: CentralAuthorizationConsent(
                        registeredClientId = registeredClientId,
                        principalName = principalName,
                        authorities = authoritiesSet
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

    override fun defineIndexes(): List<CreateIndexOperation> {
        val partialFilterSelector = DocumentSelector.partialFilterSelector(CentralAuthorizationConsent::class)

        return listOf(
            CreateIndexOperation(
                name = PK_IDX,
                index = Index(
                    fields = setOf("registeredClientId", "principalName"),
                    partialFilterSelector = partialFilterSelector
                )
            ),
            CreateIndexOperation(
                name = PRINCIPAL_IDX,
                index = Index(
                    fields = setOf("principalName"),
                    partialFilterSelector = partialFilterSelector
                )
            )
        )
    }

    private fun idQuery(
        registeredClientId: String,
        principalName: String
    ): DocumentSelector<CentralAuthorizationConsent> = DocumentSelector
        .select<CentralAuthorizationConsent>(
            "registeredClientId" to registeredClientId,
            "principalName" to principalName
        )
        .withIndex(PK_IDX)

    companion object {
        const val PRINCIPAL_IDX = "idx__centralAuthorizationConsent__principalName"
        const val PK_IDX = "idx__centralAuthorizationConsent__pk"
    }
}
