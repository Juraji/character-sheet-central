package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.services.oauth.CentralScopes
import nl.juraji.charactersheetscentral.services.oauth.support.CentralAuthorizationCreatedEvent
import nl.juraji.charactersheetscentral.services.oauth.support.CentralAuthorizationUpdatedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.stereotype.Component

@Component
class UserCouchDbMgmtListener(
    private val couchDbService: CouchDbService,
) {

    @Async
    @EventListener
    fun on(event: CentralAuthorizationCreatedEvent) = setOrUpdateDbUser(event.authorization)

    @Async
    @EventListener
    fun on(event: CentralAuthorizationUpdatedEvent) = setOrUpdateDbUser(event.authorization)

    private fun setOrUpdateDbUser(authorization: OAuth2Authorization) {
        if (authorization.accessToken == null) return

        /**
         * TODO:
         * 1 Check authorization scopes allows couch sync see [accessTokenAllowsCouchDbSync]
         *   WHEN FALSE: Remove DB user
         * 2 Get CentralUser for [authorization.principalName]
         * 2 Check user has role COUCH_DB_ACCESS
         *   WHEN FALSE: Remove DB user
         * 3 Check db user exists
         *   WHEN TRUE: Set user password to [authorization.accessToken.tokenValue]
         *   WHEN FALSE: Create DB user with [authorization.id] as username and password [authorization.accessToken.tokenValue]
         *               and add user to DB as MEMBER. Not to not override/remove any other members from the db,
         */

//        val accessToken = authorization.accessToken.token
//
//        // User never gave consent for this authorization to do CouchDB sync, exit
//        if (!accessTokenAllowsCouchDbSync(accessToken)) return
//
//        couchDbService.setUserPassword(authorization.principalName, accessToken.tokenValue)
    }

    private fun accessTokenAllowsCouchDbSync(token: OAuth2AccessToken): Boolean =
        token.scopes.contains(CentralScopes.COUCHDB_SYNC)
}
