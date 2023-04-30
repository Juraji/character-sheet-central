package nl.juraji.charactersheetscentral.configuration

import nl.juraji.charactersheetscentral.couchcb.CouchDbApiException
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.services.users.CentralUserRole
import nl.juraji.charactersheetscentral.services.users.CentralUserService
import nl.juraji.charactersheetscentral.util.catchOrThrow
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus

@Configuration
class CouchDbPrimerConfiguration(
    private val couchDb: CouchDbService,
    private val config: CentralConfiguration,
    private val usersService: CentralUserService,
) : InitializingBean {

    override fun afterPropertiesSet() {
        this.createAuthorizationsDatabase()
        this.createAdminUser()
    }

    private fun createAuthorizationsDatabase() {
        val databaseName = config.rootDbName

        // Create Database
        couchDb
            .runCatching { createDatabase(databaseName) }
            .catchOrThrow { it is CouchDbApiException && it.httpStatus == HttpStatus.PRECONDITION_FAILED }

        // @formatter:off
        // TODO Create indexes by query usage
//        couchDb.createIndex(databaseName, "idx_central_user_username", setOf("username"))
        // @formatter:on
    }

    private fun createAdminUser() {
        usersService.run {
            if (!userExists(ADMIN_USERNAME))
                createUser(ADMIN_USERNAME, ADMIN_USERNAME, CentralUserRole.ADMIN)
        }
    }

    companion object {
        const val ADMIN_USERNAME = "admin"
    }
}
