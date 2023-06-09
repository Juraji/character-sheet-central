package nl.juraji.charactersheetscentral.configuration

import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.couchdb.DocumentRepository
import nl.juraji.charactersheetscentral.couchdb.support.CouchDbApiException
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
    private val documentRepositories: List<DocumentRepository<*>>
) : InitializingBean {

    override fun afterPropertiesSet() {
        this.createRootDatabase()
        this.createAdminUser()
    }

    private fun createRootDatabase() {
        val databaseName = config.rootDbName

        // Create Database
        couchDb
            .runCatching { createDatabase(databaseName) }
            .catchOrThrow { it is CouchDbApiException && it.httpStatus == HttpStatus.PRECONDITION_FAILED }

        documentRepositories
            .flatMap(DocumentRepository<*>::defineIndexes)
            .map { it.copy(designDocumentName = config.indexDesignDocument) }
            .forEach { couchDb.createIndex(databaseName, it) }
    }

    private fun createAdminUser() {
        usersService.run {
            if (!userExists(config.adminUsername))
                createUser(
                    config.adminUsername,
                    config.adminPassword,
                    CentralUserRole.ADMIN,
                )
        }
    }
}
