package nl.juraji.charactersheetscentral.couchcb

import nl.juraji.charactersheetscentral.couchcb.find.ApiFindOperationResult
import nl.juraji.charactersheetscentral.couchcb.find.FindQuery
import nl.juraji.charactersheetscentral.couchcb.support.ApiDocumentOperationResult
import nl.juraji.charactersheetscentral.couchcb.support.DocumentIdMeta
import org.springframework.core.ParameterizedTypeReference
import kotlin.reflect.KClass

abstract class CouchDbDocumentRepository<T : DocumentIdMeta>(
    private val couchDb: CouchDbService
) {
    abstract val databaseName: String
    abstract val documentClass: KClass<T>
    abstract val documentFindTypeRef: ParameterizedTypeReference<ApiFindOperationResult<T>>

    fun findDocumentById(documentId: String): T? =
        couchDb.findDocumentById(databaseName, documentId, documentClass)

    fun findOneDocumentBySelector(findQuery: FindQuery): T? =
        couchDb.findOneDocumentBySelector(databaseName, findQuery.singleResult(), documentFindTypeRef)

    fun findDocumentBySelector(findQuery: FindQuery): List<T> =
        couchDb.findDocumentBySelector(databaseName, findQuery, documentFindTypeRef)

    fun saveDocument(document: T): ApiDocumentOperationResult =
        couchDb.saveDocument(databaseName, document)

    fun deleteDocument(document: T) =
        couchDb.deleteDocument(databaseName, document)

    fun deleteDocument(documentId: String, documentRev: String) =
        couchDb.deleteDocument(databaseName, documentId, documentRev)
}
