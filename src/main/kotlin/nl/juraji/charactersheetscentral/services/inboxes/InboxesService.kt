package nl.juraji.charactersheetscentral.services.inboxes

import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.services.users.CentralUserService
import org.springframework.stereotype.Service

@Service
class InboxesService(
    private val couchDbService: CouchDbService,
    private val centralUserService: CentralUserService
) {
    fun runInboxes() {
        val allUsers = centralUserService.findAll()

        TODO()
    }
}
