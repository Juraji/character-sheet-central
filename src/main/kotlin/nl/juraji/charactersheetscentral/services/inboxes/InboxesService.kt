package nl.juraji.charactersheetscentral.services.inboxes

import com.fasterxml.jackson.databind.ObjectMapper
import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocumentMetaData
import nl.juraji.charactersheetscentral.couchdb.find.FindQuery
import nl.juraji.charactersheetscentral.couchdb.find.FindResult
import nl.juraji.charactersheetscentral.couchdb.find.eq
import nl.juraji.charactersheetscentral.couchdb.find.modelQuery
import nl.juraji.charactersheetscentral.services.users.CentralUser
import nl.juraji.charactersheetscentral.services.users.CentralUserRole
import nl.juraji.charactersheetscentral.services.users.CentralUserService
import nl.juraji.charactersheetscentral.util.LoggerCompanion
import nl.juraji.charactersheetscentral.util.jackson.restTemplateTypeRef
import nl.juraji.charactersheetscentral.util.then
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class InboxesService(
    private val config: CentralConfiguration,
    private val objectMapper: ObjectMapper,
    private val couchDbService: CouchDbService,
    private val centralUserService: CentralUserService,
) {
    // TODO: Can we go multi-threaded?
    private val executorService = Executors.newSingleThreadExecutor()
    private val outboxTypeRef get() = restTemplateTypeRef<FindResult<OutboxMessage>>()

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    fun runInboxes() {
        logger.debug("Running inboxes...")
        centralUserService
            .findByRoleOrAuthority(CentralUserRole.INBOXES_SEND)
            .forEach { executorService.submit(this.checkAndSendNewOutboxMessages(it)) }
    }

    private fun checkAndSendNewOutboxMessages(sender: CentralUser): () -> Unit = {
        val senderDbName = config.userDbName(sender.username)
        val messageQuery: FindQuery<OutboxMessage> = modelQuery(eq("status", OutBoxSendStatus.NEW))

        val outboxMessages = couchDbService.findDocumentBySelector(senderDbName, messageQuery, outboxTypeRef)

        logger.debug("Found ${outboxMessages.size} new outbox messages for user ${sender.username}")

        outboxMessages.forEach { outboxMsg ->
            runCatching { sendOutboxMessage(sender, senderDbName, outboxMsg) }
                .onFailure { e ->
                    logger.warn("Failed to send outbox message with id ${outboxMsg.id} from user ${sender.username}", e)

                    /** Update msg status with error */
                    val status = when (e) {
                        is UsernameNotFoundException -> OutBoxSendStatus.FAILED_RECEIVER_UNKNOWN
                        else -> OutBoxSendStatus.FAILED_UNKNOWN_ERROR
                    }

                    couchDbService.saveDocument(senderDbName, outboxMsg.copy(status = status))
                }
                .onSuccess {
                    logger.debug("Outbox message with id ${outboxMsg.id} from user ${sender.username} sent successfully")
                    /** Update msg status to SENT */
                    couchDbService.saveDocument(senderDbName, outboxMsg.copy(status = OutBoxSendStatus.SENT))
                }
        }
    }

    private fun sendOutboxMessage(sender: CentralUser, senderDbName: String, outboxMessage: OutboxMessage) {
        if (!centralUserService.userExists(outboxMessage.receiver))
            throw UsernameNotFoundException("User with name ${outboxMessage.receiver} does not exist")

        val receiverDbName = config.userDbName(outboxMessage.receiver)

        val contentDocs: List<InboxDocument> = outboxMessage.documentIds
            .mapNotNull { couchDbService.findRawDocumentById(senderDbName, it) }
            .associateBy { objectMapper.readValue(it, CentralDocumentMetaData::class.java) }
            .map { (meta, raw) ->
                val attachmentsMap = meta.attachments
                    .map { (name, stub) -> "${meta.id}::$name" to stub.copy(name = name) }
                    .toMap()

                InboxDocument(meta.id!!, meta.rev!!, meta.modelType, raw, attachmentsMap)
            }

        val inboxMessage = InboxMessage(
            sender = sender.username,
            message = outboxMessage.message,
            documents = contentDocs
        )

        // Save inbox document to receiver db
        couchDbService.saveDocument(receiverDbName, inboxMessage)
        contentDocs
            .flatMap { doc -> doc.attachmentStubs.entries.map { doc to it.key then it.value } }
            .forEach { (doc, key, stub) ->
                couchDbService.run {
                    val resource = getDocumentAttachment(senderDbName, doc.id, doc.rev, stub.name!!)
                    addDocumentAttachment(receiverDbName, doc.id, doc.rev, key, resource)
                }
            }
    }

    companion object : LoggerCompanion(InboxesService::class)
}


