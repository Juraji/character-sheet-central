package nl.juraji.charactersheetscentral.services.oauth.support

import org.springframework.context.ApplicationEvent
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization

data class CentralAuthorizationUpdatedEvent(
    val authorization: OAuth2Authorization
) : ApplicationEvent(authorization)
