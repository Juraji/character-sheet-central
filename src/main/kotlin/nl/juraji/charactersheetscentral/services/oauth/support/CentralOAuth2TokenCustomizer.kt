package nl.juraji.charactersheetscentral.services.oauth.support

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.stereotype.Component

@Component
class CentralOAuth2TokenCustomizer(
    private val config: CentralConfiguration,
) : OAuth2TokenCustomizer<JwtEncodingContext> {

    override fun customize(context: JwtEncodingContext) {
        when (context.tokenType.value) {
            OidcParameterNames.ID_TOKEN -> augmentIdToken(context)
        }
    }

    private fun augmentIdToken(context: JwtEncodingContext) {
        context.authorization?.let {
            context.claims.claim(CLAIM_COUCH_DB_USERNAME, it.id)
            context.claims.claim(CLAIM_COUCH_DB_NAME, config.userDbPrefix + it.principalName)
        }
        context.getPrincipal<Authentication>().authorities
            .map(GrantedAuthority::getAuthority)
            .let { context.claims.claim(CLAIM_AUTHORITIES, it) }
    }

    companion object {
        const val CLAIM_AUTHORITIES = "authorities"
        const val CLAIM_COUCH_DB_NAME = "couchdb_name"
        const val CLAIM_COUCH_DB_USERNAME = "couchdb_username"
    }
}
