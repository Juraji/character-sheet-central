package nl.juraji.charactersheetscentral.services.inboxes

import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.services.users.CentralUserService
import nl.juraji.charactersheetscentral.util.LoggerCompanion
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class InboxesService(
    private val couchDbService: CouchDbService,
    private val centralUserService: CentralUserService
) {

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    fun runInboxes() {
        LOGGER.info("RUNNING INBOXES...")
//        val allUsers = centralUserService.findAll()
    }

    companion object : LoggerCompanion(InboxesService::class)
}
