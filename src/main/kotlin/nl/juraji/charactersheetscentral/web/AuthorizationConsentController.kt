package nl.juraji.charactersheetscentral.web

import nl.juraji.charactersheetscentral.util.l
import org.springframework.context.MessageSource
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.security.Principal


@Controller
class AuthorizationConsentController(
    private val registeredClientRepository: RegisteredClientRepository,
    private val authorizationConsentService: OAuth2AuthorizationConsentService,
    private val messageSource: MessageSource,
) {
    @RequestMapping(value = ["/oauth2/consent"])
    fun consent(
        principal: Principal,
        model: Model,
        @RequestParam(OAuth2ParameterNames.CLIENT_ID) clientId: String,
        @RequestParam(OAuth2ParameterNames.SCOPE) scope: String,
        @RequestParam(OAuth2ParameterNames.STATE) state: String
    ): String? {

        // Remove scopes that were already approved
        val registeredClient = registeredClientRepository.findByClientId(clientId)
        val previouslyApprovedScopes = authorizationConsentService
            .findById(registeredClient!!.id, principal.name)
            ?.scopes
            ?.minus(OidcScopes.OPENID)
            ?: emptySet()

        val scopesToApprove = scope
            .split(' ')
            .minus(OidcScopes.OPENID)
            .subtract(previouslyApprovedScopes)


        model.addAttribute("clientId", clientId)
        model.addAttribute("state", state)
        model.addAttribute("scopes", withDescription(scopesToApprove))
        model.addAttribute("previouslyApprovedScopes", withDescription(previouslyApprovedScopes))

        return "consent"
    }

    private fun withDescription(scopes: Set<String>): Set<ScopeWithDescription> = scopes
        .associateWith { messageSource.l("consent.scopeDescription.$it") }
        .map { (scope, description) -> ScopeWithDescription(scope, description) }
        .toSet()

    data class ScopeWithDescription(
        val scope: String,
        val description: String
    )
}
