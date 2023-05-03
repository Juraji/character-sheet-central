package nl.juraji.charactersheetscentral.services.oauth.support

import org.springframework.context.ApplicationEvent
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization

/**
 * This event is triggered when either the [OAuth2Authorization.getAccessToken] is invalidated
 * OR the [OAuth2Authorization] is deleted!
 */
data class CentralAuthorizationInvalidatedEvent(
    val authorization: OAuth2Authorization
) : ApplicationEvent(authorization)
