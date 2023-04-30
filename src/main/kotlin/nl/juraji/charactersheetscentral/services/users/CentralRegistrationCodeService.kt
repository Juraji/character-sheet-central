package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.configuration.CouchCbConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Repository
import java.security.SecureRandom
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.streams.asSequence

@Repository
class CentralRegistrationCodeService(
    configuration: CouchCbConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralRegistrationCode>(couchDb) {
    override val databaseName: String = configuration.authorizationsDatabaseName
    override val documentClass: KClass<CentralRegistrationCode> = CentralRegistrationCode::class
    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralRegistrationCode>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralRegistrationCode>>() {}

    fun findRegistrationCode(code: String): CentralRegistrationCode? {
        val nowMillis = Instant.now().toEpochMilli()
        val selector = DocumentSelector.select(
            "code" to code,
            "expiresAt" to mapOf(DocumentSelector.OP_GT to nowMillis)
        )

        // If there is a document for the above selector the code is valid
        return findOneDocumentBySelector(selector)
    }

    fun createRegistrationCode(name: String): CentralRegistrationCode {
        val registrationCode = CentralRegistrationCode(
            name = name,
            code = generateCode(),
            expiresAt = Instant.now().plusSeconds(CODE_TTL)
        )

        val (id, rev) = saveDocument(registrationCode)

        return registrationCode.copy(id = id, rev = rev)
    }

    private fun generateCode(): String {
        return SecureRandom()
            .ints(CODE_CHUNK_SIZE * CODE_NO_OF_CHUNKS, 0, CODE_CHAR_SRC.length)
            .asSequence()
            .map(CODE_CHAR_SRC::get)
            .chunked(CODE_CHUNK_SIZE)
            .map { it.joinToString("") }
            .joinToString("-")
    }

    companion object {
        const val CODE_CHAR_SRC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        const val CODE_CHUNK_SIZE = 4
        const val CODE_NO_OF_CHUNKS = 4L
        const val CODE_TTL = 86400L // 1 day
    }
}
