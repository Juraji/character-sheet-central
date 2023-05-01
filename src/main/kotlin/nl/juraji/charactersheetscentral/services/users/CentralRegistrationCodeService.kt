package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.CreateIndexOperation
import nl.juraji.charactersheetscentral.couchcb.support.Index
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Repository
import java.security.SecureRandom
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.streams.asSequence

@Repository
class CentralRegistrationCodeService(
    configuration: CentralConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralRegistrationCode>(couchDb) {
    override val databaseName: String = configuration.rootDbName
    override val documentClass: KClass<CentralRegistrationCode> = CentralRegistrationCode::class
    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralRegistrationCode>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralRegistrationCode>>() {}

    fun findRegistrationCode(code: String): CentralRegistrationCode? {
        val nowMillis = Instant.now().toEpochMilli()
        val selector = DocumentSelector
            .select<CentralRegistrationCode>(
                "code" to code,
                "expiresAt" to mapOf(DocumentSelector.Match.GT to nowMillis)
            )
            .withIndex(CODE_IDX)

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

    override fun defineIndexes(): List<CreateIndexOperation> = listOf(
        CreateIndexOperation(
            name = CODE_IDX,
            index = Index(
                fields = setOf("code", "expiresAt"),
                partialFilterSelector = DocumentSelector
                    .partialFilterSelector(CentralRegistrationCode::class)
            )
        )
    )

    companion object {
        const val CODE_CHAR_SRC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        const val CODE_CHUNK_SIZE = 4
        const val CODE_NO_OF_CHUNKS = 4L
        const val CODE_TTL = 86400L // 1 day

        const val CODE_IDX = "idx__centralRegistrationCode__code_expiresAt"
    }
}
