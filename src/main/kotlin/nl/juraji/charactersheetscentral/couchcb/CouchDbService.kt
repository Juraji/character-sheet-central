package nl.juraji.charactersheetscentral.couchcb

import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.*
import nl.juraji.charactersheetscentral.util.assertNotNull
import nl.juraji.charactersheetscentral.util.l
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
    private val genericTypeRef: ParameterizedTypeReference<ApiFindResult<CentralDocumentMetaData>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralDocumentMetaData>>() {}

    // Documents
    fun <T : CentralDocument> findDocumentById(databaseName: String, documentId: String, documentClass: KClass<T>): T? =
        restTemplate.getForObject("/$databaseName/$documentId", documentClass.java)

    fun <T : CentralDocument> findOneDocumentBySelector(
        databaseName: String,
        query: DocumentSelector<T>,
        typeReference: ParameterizedTypeReference<ApiFindResult<T>>
    ): T? = findDocumentBySelector(databaseName, query.singleResult(), typeReference).firstOrNull()

    fun documentExistsBySelector(databaseName: String, query: DocumentSelector<*>): Boolean {
        // This is a hack, as the input query type will never match the generic type reference used for deserialization.
        // But this does not matter, as we never actually need the list contents, just whether it has content or not.
        @Suppress("UNCHECKED_CAST")
        query as DocumentSelector<CentralDocumentMetaData>
        // Selects the minimal fields required to optimize query
        return findDocumentBySelector(databaseName, query.withFields().singleResult(), genericTypeRef).isNotEmpty()
    }

    fun <T : CentralDocument> findDocumentBySelector(
        databaseName: String,
        query: DocumentSelector<T>,
        typeReference: ParameterizedTypeReference<ApiFindResult<T>>
    ): List<T> {
        val uri = "/$databaseName/_find"
        val request = HttpEntity(query)

        return restTemplate
            .exchange(uri, HttpMethod.POST, request, typeReference)
            .body?.docs
            ?: emptyList()
    }

    fun <T : CentralDocument> saveDocument(
        databaseName: String,
        document: T,
        action: SaveAction = SaveAction.AUTO
    ): ApiDocumentOperationResult {
        val (id, rev) = document

        return when (action) {
            SaveAction.AUTO ->
                if (id == null) createDocument(databaseName, document)
                else updateDocument(databaseName, id, rev, document)

            SaveAction.CREATE -> createDocument(databaseName, document)
            SaveAction.UPDATE -> updateDocument(databaseName, id, rev, document)
        }
    }

    private fun <T : CentralDocument> createDocument(databaseName: String, document: T): ApiDocumentOperationResult {
        val newDocId = document.id ?: UUID.randomUUID().toString()
        val uri = "/$databaseName/$newDocId"
        val request = HttpEntity(document)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, ApiDocumentOperationResult::class.java)
            .orThrowNotFound(databaseName, null)
    }

    private fun <T : CentralDocument> updateDocument(
        databaseName: String,
        documentId: String?,
        documentRev: String?,
        document: T
    ): ApiDocumentOperationResult {
        assertNotNull(documentId) { messageSource.l("couchDbService.assertions.existingDocIdMissing") }
        assertNotNull(documentRev) { messageSource.l("couchDbService.assertions.existingDocRevMissing") }

        val uri = "/$databaseName/$documentId"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, documentRev)
        }
        val request = HttpEntity(document, headers)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, ApiDocumentOperationResult::class.java)
            .orThrowNotFound(databaseName, document.id)
    }

    fun deleteDocument(databaseName: String, document: DocumentIdMeta) {
        val (id, rev) = document

        assertNotNull(id) { messageSource.l("couchDbService.assertions.deleteMissingId") }
        assertNotNull(rev) { messageSource.l("couchDbService.assertions.deleteMissingRev") }

        deleteDocument(databaseName, id, rev)
    }

    fun deleteDocument(databaseName: String, documentId: String, documentRev: String) {
        val uri = "/$databaseName/$documentId"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, documentRev)
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

        restTemplate.put(uri, op)
    }

    fun findUser(username: String): CouchDbUserDocument? =
        findDocumentById("_users", "org.couchdb.user:$username", CouchDbUserDocument::class)

    fun addUser(
        username: String,
        password: String = "NOOP_PW-${UUID.randomUUID()}"
    ): ApiDocumentOperationResult {
        val doc = CouchDbUserDocument(
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
        .let { updateDocument("_users", it.id, it.rev, it) }

    fun removeMemberUser(username: String) =
        findUser(username)?.let { deleteDocument("_users", it) }


    // Indexes
    fun createIndex(
        databaseName: String,
        indexName: String,
        fields: Set<String>,
        partialFilterSelector: DocumentSelector<CentralDocumentMetaData>? = null
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
        .l("couchDbApi.responses.notFound", databaseName, forDocumentId ?: "[UNKNOWN ID]")
        .run { throw CouchDbApiException(HttpStatus.NOT_FOUND, this) }
        // @formatter:on

}

