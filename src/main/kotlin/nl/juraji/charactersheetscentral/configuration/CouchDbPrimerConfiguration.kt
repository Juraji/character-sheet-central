package nl.juraji.charactersheetscentral.configuration

import nl.juraji.charactersheetscentral.couchcb.CouchDbApiException
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.util.catchOrThrow
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus

@Configuration
class CouchDbPrimerConfiguration(
    private val couchDb: CouchDbService,
    private val configuration: CouchCbConfiguration,
) : InitializingBean {

    override fun afterPropertiesSet() {
        this.createAuthorizationsDatabase()
    }

    private fun createAuthorizationsDatabase() {
        val databaseName = configuration.authorizationsDatabaseName

        // Create Database
        couchDb
            .runCatching { createDatabase(databaseName) }
            .catchOrThrow { it is CouchDbApiException && it.httpStatus == HttpStatus.PRECONDITION_FAILED }

        // @formatter:off
        couchDb.createIndex(databaseName, "idx_central_user_username", setOf("username"))
        couchDb.createIndex(databaseName, "idx_central_oauth_authorization_state", setOf("state"))
        couchDb.createIndex(databaseName, "idx_central_oauth_authorization_authorization_code_token_value", setOf("authorizationCode.tokenValue"))
        couchDb.createIndex(databaseName, "idx_central_oauth_authorization_access_token_token_value", setOf("accessToken.tokenValue"))
        couchDb.createIndex(databaseName, "idx_central_oauth_authorization_refresh_token_token_value", setOf("refreshToken.tokenValue"))
        couchDb.createIndex(databaseName, "idx_central_oauth_authorization_all_tokens_value_token_value", setOf("authorizationCode.tokenValue", "accessToken.tokenValue", "refreshToken.tokenValue"))
        couchDb.createIndex(databaseName, "idx_central_authorization_consent_pk", setOf("registeredClientId", "principalName"))
        // @formatter:on
    }
}
