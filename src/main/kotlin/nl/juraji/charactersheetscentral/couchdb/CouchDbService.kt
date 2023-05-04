package nl.juraji.charactersheetscentral.couchdb

import nl.juraji.charactersheetscentral.couchdb.documents.*
import nl.juraji.charactersheetscentral.couchdb.find.FindQuery
import nl.juraji.charactersheetscentral.couchdb.find.FindResult
import nl.juraji.charactersheetscentral.couchdb.find.singleResult
import nl.juraji.charactersheetscentral.couchdb.find.withFields
import nl.juraji.charactersheetscentral.couchdb.indexes.CreateIndexOp
import nl.juraji.charactersheetscentral.couchdb.indexes.IndexOpResult
import nl.juraji.charactersheetscentral.couchdb.support.*
import nl.juraji.charactersheetscentral.couchdb.users.DatabaseMembersDocument
import nl.juraji.charactersheetscentral.couchdb.users.UserDocument
import nl.juraji.charactersheetscentral.util.assertNotNull
import nl.juraji.charactersheetscentral.util.jackson.restTemplateTypeRef
import nl.juraji.charactersheetscentral.util.l
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.Resource
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
    private val genericTypeRef: ParameterizedTypeReference<FindResult<CentralDocumentMetaData>>
        get() = restTemplateTypeRef<FindResult<CentralDocumentMetaData>>()

    // Documents
    fun <T : CouchDbDocument> findDocumentById(databaseName: String, documentId: String, documentClass: KClass<T>): T? =
        restTemplate.getForObject("/$databaseName/$documentId", documentClass.java)

    fun findRawDocumentById(databaseName: String, documentId: String): String? =
        restTemplate.getForObject("/$databaseName/$documentId", String::class.java)

    fun <T : CouchDbDocument> findOneDocumentBySelector(
        databaseName: String,
        query: FindQuery<T>,
        typeReference: ParameterizedTypeReference<FindResult<T>>
    ): T? = findDocumentBySelector(databaseName, query.singleResult(), typeReference).firstOrNull()

    fun documentExistsBySelector(databaseName: String, query: FindQuery<*>): Boolean {
        // This is a hack, as the input query type will never match the generic type reference used for deserialization.
        // But this does not matter, as we never actually need the list contents, just whether it has content or not.
        @Suppress("UNCHECKED_CAST")
        query as FindQuery<CentralDocumentMetaData>
        // Selects the minimal fields required to optimize query
        return findDocumentBySelector(databaseName, query.withFields().singleResult(), genericTypeRef).isNotEmpty()
    }

    fun <T : CouchDbDocument> findDocumentBySelector(
        databaseName: String,
        query: FindQuery<T>,
        typeReference: ParameterizedTypeReference<FindResult<T>>
    ): List<T> {
        val uri = "/$databaseName/_find"
        val request = HttpEntity(query)

        return restTemplate
            .exchange(uri, HttpMethod.POST, request, typeReference)
            .body?.docs
            ?: emptyList()
    }

    fun <T : CouchDbDocument> saveDocument(
        databaseName: String,
        document: T,
        action: SaveType = SaveType.AUTO
    ): DocumentOpResult {
        val (id, rev) = document

        return when (action) {
            SaveType.AUTO ->
                if (id == null) createDocument(databaseName, document)
                else updateDocument(databaseName, id, rev, document)

            SaveType.CREATE -> createDocument(databaseName, document)
            SaveType.UPDATE -> updateDocument(databaseName, id, rev, document)
        }
    }

    private fun <T : CouchDbDocument> createDocument(databaseName: String, document: T): DocumentOpResult {
        val newDocId = document.id ?: UUID.randomUUID().toString()
        val uri = "/$databaseName/$newDocId"
        val request = HttpEntity(document)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, DocumentOpResult::class.java)
            .orThrowNotFound(databaseName, "[NEW]")
    }

    private fun <T : CouchDbDocument> updateDocument(
        databaseName: String,
        documentId: String?,
        documentRev: String?,
        document: T
    ): DocumentOpResult {
        assertNotNull(documentId) { messageSource.l("couchDbService.assertions.existingDocIdMissing") }
        assertNotNull(documentRev) { messageSource.l("couchDbService.assertions.existingDocRevMissing") }

        val uri = "/$databaseName/$documentId"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, documentRev)
        }
        val request = HttpEntity(document, headers)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, DocumentOpResult::class.java)
            .orThrowNotFound(databaseName, documentId)
    }

    fun deleteDocument(databaseName: String, document: CouchDbDocument) {
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

        restTemplate.exchange(uri, HttpMethod.DELETE, request, DocumentOpResult::class.java)
    }

    // Attachments
    fun getDocumentAttachment(
        databaseName: String,
        documentId: String,
        documentRev: String,
        attachmentName: String
    ): Resource {
        val uri = "/$databaseName/$documentId/$attachmentName"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, documentRev)
        }
        val request = HttpEntity(null, headers)

        return restTemplate
            .exchange(uri, HttpMethod.GET, request, Resource::class.java)
            .orThrowNotFound(databaseName, attachmentName)
    }

    fun addDocumentAttachment(
        databaseName: String,
        documentId: String,
        documentRev: String,
        attachmentName: String,
        resource: Resource
    ): DocumentOpResult {
        val uri = "/$databaseName/$documentId/$attachmentName"
        val headers = HttpHeaders().apply {
            set(HttpHeaders.IF_MATCH, documentRev)
        }
        val request = HttpEntity(resource, headers)

        return restTemplate
            .exchange(uri, HttpMethod.PUT, request, DocumentOpResult::class.java)
            .orThrowNotFound(databaseName, documentId)
    }

    // Database meta data
    fun createDatabase(databaseName: String) {
        val uri = "/$databaseName"
        restTemplate.exchange(uri, HttpMethod.PUT, null, CouchOperationError::class.java)
    }

    fun deleteDatabase(databaseName: String) {
        val uri = "/$databaseName"
        restTemplate.exchange(uri, HttpMethod.DELETE, null, CouchOperationError::class.java)
    }

    fun getDatabaseUsers(databaseName: String): DatabaseMembersDocument {
        val uri = "/$databaseName/_security"
        return restTemplate
            .getForObject(uri, DatabaseMembersDocument::class.java)
            ?: throwNotFound(databaseName, "DatabaseMembersDocument")
    }

    fun setDatabaseUsers(
        databaseName: String,
        membersDocument: DatabaseMembersDocument
    ) {
        val uri = "/$databaseName/_security"
        restTemplate.put(uri, membersDocument)
    }

    fun findUser(username: String): UserDocument? =
        findDocumentById("_users", "org.couchdb.user:$username", UserDocument::class)

    fun addUser(
        username: String,
        password: String = "NOOP_PW-${UUID.randomUUID()}"
    ): DocumentOpResult {
        val doc = UserDocument(
            id = "org.couchdb.user:$username",
            name = username,
            password = password
        )

        return createDocument("_users", doc)
    }

    fun setUserPassword(
        username: String,
        password: String
    ): DocumentOpResult = findUser(username)
        ?.run { copy(password = password) }
        ?.let { updateDocument("_users", it.id, it.rev, it) }
        ?: throwNotFound("_users", username)

    fun removeUser(username: String) =
        findUser(username)?.let { deleteDocument("_users", it) }


    // Indexes
    fun createIndex(
        databaseName: String,
        createIndexOp: CreateIndexOp,
    ) {
        val uri = "/$databaseName/_index"
        restTemplate.postForEntity(uri, createIndexOp, IndexOpResult::class.java)
    }

    private fun throwNotFound(databaseName: String, subject: String?): Nothing = messageSource
        .l("couchDbApi.responses.notFound", databaseName, subject ?: "[UNKNOWN ID]")
        .run { throw CouchDbApiException(HttpStatus.NOT_FOUND, this) }

    protected fun <R, E : ResponseEntity<R>> E.orThrowNotFound(databaseName: String, subject: String?): R =
        body ?: throwNotFound(databaseName, subject)

}

