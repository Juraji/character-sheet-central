package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.couchdb.users.addMemberName
import nl.juraji.charactersheetscentral.services.oauth.CentralScopes
import nl.juraji.charactersheetscentral.services.oauth.support.CentralAuthorizationInvalidatedEvent
import nl.juraji.charactersheetscentral.services.oauth.support.CentralAuthorizationUpdatedEvent
import nl.juraji.charactersheetscentral.services.oauth.support.CentralOAuth2TokenCustomizer
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.stereotype.Component

@Component
class UserCouchDbMgmtListener(
    private val couchDbService: CouchDbService,
) {

    @Async
    @EventListener
    fun on(event: CentralAuthorizationUpdatedEvent) {
        if (event.authorization.accessToken == null) return
        val accessToken = event.authorization.accessToken.token
        val oidcToken = event.authorization.getToken(OidcIdToken::class.java)!!

        val authorities: List<String> = oidcToken.getClaim(CentralOAuth2TokenCustomizer.CLAIM_AUTHORITIES)
        val couchDbName: String = oidcToken.getClaim(CentralOAuth2TokenCustomizer.CLAIM_COUCH_DB_NAME)
        val couchUsername: String = oidcToken.getClaim(CentralOAuth2TokenCustomizer.CLAIM_COUCH_DB_USERNAME)
        val couchUserPassword: String = accessToken.tokenValue

        // User never gave consent for this authorization to do CouchDB sync, exit
        // User does not have the right authority to do CouchDbSync
        if (userNotHasCouchDbSyncAccess(accessToken.scopes, authorities)) {
            couchDbService.removeUser(couchUsername)
            return
        }

        // Upsert CouchDb user with new password
        couchDbService.run {
            findUser(couchUsername)
                ?.let { setUserPassword(couchUsername, couchUserPassword) }
                ?: addUser(couchUsername, couchUserPassword)
        }

        // Ensure CouchDB user has access to [couchDbName]
        couchDbService.run {
            getDatabaseUsers(couchDbName)
                .takeUnless { it.members.names.contains(couchUsername) }
                ?.addMemberName(couchUsername)
                ?.let { setDatabaseUsers(couchDbName, it) }
        }
    }

    @Async
    @EventListener
    fun on(event: CentralAuthorizationInvalidatedEvent) {
        TODO()
    }

    /**
     * Returns TRUE if:
     * - [scopes] does not contain the [CentralScopes.COUCHDB_SYNC] scope
     * - [authorities] does not contain the [CentralUserRole.COUCH_DB_ACCESS] role
     */
    private fun userNotHasCouchDbSyncAccess(scopes: Set<String>, authorities: List<String>): Boolean {
        return !scopes.contains(CentralScopes.COUCHDB_SYNC)
                || !authorities.contains("ROLE_${CentralUserRole.COUCH_DB_ACCESS}")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : OAuth2Token, R> OAuth2Authorization.Token<T>.getClaim(name: String): R = this.claims!![name] as R
}
