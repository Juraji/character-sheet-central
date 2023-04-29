package nl.juraji.charactersheetscentral.couchcb

import nl.juraji.charactersheetscentral.couchcb.find.ApiFindOperationResult
import nl.juraji.charactersheetscentral.couchcb.find.FindQuery
import nl.juraji.charactersheetscentral.couchcb.support.*
import nl.juraji.charactersheetscentral.util.assertNotNull
import nl.juraji.charactersheetscentral.util.assertNull
import nl.juraji.charactersheetscentral.util.localizedMessage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*
import kotlin.reflect.KClass

@Service
class CouchDbService(
    @Qualifier("couchDbRestTemplate") protected val restTemplate: RestTemplate,
    private val messageSource: MessageSource
) {
    // Documents
    fun <T : DocumentIdMeta> findDocumentById(databaseName: String, documentId: String, documentClass: KClass<T>): T? =
        restTemplate.getForObject("/$databaseName/$documentId", documentClass.java)

    fun <T : DocumentIdMeta> findOneDocumentBySelector(
        databaseName: String,
        findQuery: FindQuery,
        typeReference: ParameterizedTypeReference<ApiFindOperationResult<T>>
    ): T? = findDocumentBySelector(databaseName, findQuery.singleResult(), typeReference).firstOrNull()

    fun <T : DocumentIdMeta> findDocumentBySelector(
        databaseName: String,
        findQuery: FindQuery,
        typeReference: ParameterizedTypeReference<ApiFindOperationResult<T>>
    ): List<T> {
        val uri = "/$databaseName/_find"
        val request = HttpEntity(findQuery)

        return restTemplate
            .exchange(uri, HttpMethod.POST, request, typeReference)
            .body?.docs
            ?: emptyList()
    }

    fun <T : DocumentIdMeta> saveDocument(databaseName: String, document: T): ApiDocumentOperationResult =
        if (document.id == null) createDocument(databaseName, document)
        else updateDocument(databaseName, document)

    fun <T : DocumentIdMeta> createDocument(databaseName: String, document: T): ApiDocumentOperationResult {
        assertNull(document.rev) { messageSource.localizedMessage("nl.juraji.CouchDbService.assertions.newDocWithRev") }

        val newDocId = document.id ?: UUID.randomUUID().toString()
        val uri = "/$databaseName/$newDocId"
        val request = HttpEntity(document)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, ApiDocumentOperationResult::class.java)
            .orThrowNotFound(databaseName, null)
    }

    fun <T : DocumentIdMeta> updateDocument(databaseName: String, document: T): ApiDocumentOperationResult {
        assertNotNull(document.id) { messageSource.localizedMessage("nl.juraji.CouchDbService.assertions.existingDocIdMissing") }
        assertNotNull(document.rev) { messageSource.localizedMessage("nl.juraji.CouchDbService.assertions.existingDocRevMissing") }

        val uri = "/$databaseName/${document.id}"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, document.rev)
        }
        val request = HttpEntity(document, headers)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, ApiDocumentOperationResult::class.java)
            .orThrowNotFound(databaseName, document.id)
    }

    fun deleteDocument(databaseName: String, document: DocumentIdMeta) {
        val (id, rev) = document

        assertNotNull(id) { messageSource.localizedMessage("nl.juraji.CouchDbService.assertions.deleteMissingId") }
        assertNotNull(rev) { messageSource.localizedMessage("nl.juraji.CouchDbService.assertions.deleteMissingRev") }

        deleteDocument(databaseName, id, rev)
    }

    fun deleteDocument(databaseName: String, documentId: String, documentRev: String) {
        val uri = "/$databaseName/$documentId"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, documentRev)
            accept = listOf(MediaType.APPLICATION_JSON)
        }
        val request = HttpEntity(null, headers)

        restTemplate.exchange(uri, HttpMethod.DELETE, request, ApiDocumentOperationResult::class.java)
    }

    // Database meta data
    fun createDatabase(databaseName: String) {
        val uri = "/$databaseName"
        restTemplate.exchange(uri, HttpMethod.PUT, null, ApiOperationError::class.java)
    }

    fun deleteDatabase(databaseName: String) {
        val uri = "/$databaseName"
        restTemplate.exchange(uri, HttpMethod.DELETE, null, ApiOperationError::class.java)
    }

    fun setDatabaseUsers(
        databaseName: String,
        admins: Set<String> = emptySet(),
        members: Set<String> = emptySet(),
    ) {
        val uri = "/$databaseName/_security"
        val op = SetDatabaseUsersOperation(
            admins = DatabaseUsers(admins),
            members = DatabaseUsers(members)
        )

        val request = HttpEntity(op)

        restTemplate
            .exchange(uri, HttpMethod.PUT, request, ApiOperationError::class.java)
            .orThrowNotFound(databaseName, "_security")
    }

    fun findUser(username: String): UserDocument? =
        findDocumentById("_users", "org.couchdb.user:$username", UserDocument::class)

    fun addUser(
        username: String,
        password: String = "NOOP_PW-${UUID.randomUUID()}"
    ): ApiDocumentOperationResult {
        val doc = UserDocument(
            id = "org.couchdb.user:$username",
            name = username,
            password = password
        )

        return createDocument("_users", doc)
    }

    fun setMemberUserPassword(
        username: String,
        password: String
    ): ApiDocumentOperationResult = findUser(username)
        .orThrowNotFound("_users", username)
        .run { copy(password = password) }
        .let { updateDocument("_users", it) }

    fun removeMemberUser(username: String) =
        findUser(username)?.let { deleteDocument("_users", it) }


    // Indexes
    fun createIndex(
        databaseName: String,
        indexName: String,
        fields: Set<String>,
        partialFilterSelector: FindQuery? = null
    ) {
        val uri = "/$databaseName/_index"
        val body = CreateIndexOperation(
            indexName = indexName,
            index = Index(
                fields = fields,
                partialFilterSelector = partialFilterSelector
            )
        )

        restTemplate.postForEntity(uri, body, ApiIndexOperationResult::class.java)
    }

    protected fun <R, E : ResponseEntity<R>> E.orThrowNotFound(databaseName: String, forDocumentId: String?): R =
        body.orThrowNotFound(databaseName, forDocumentId)

    protected fun <R : Any> R?.orThrowNotFound(databaseName: String, forDocumentId: String?): R = this ?: messageSource
        // @formatter:off
        .localizedMessage("nl.juraji.CouchDbService.couchApiResponse.notFound", databaseName, forDocumentId ?: "[UNKNOWN ID]")
        .run { throw CouchDbApiException(HttpStatus.NOT_FOUND, this) }
        // @formatter:on

}

