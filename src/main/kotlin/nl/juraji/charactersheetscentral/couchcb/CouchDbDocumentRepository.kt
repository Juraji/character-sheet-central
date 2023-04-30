package nl.juraji.charactersheetscentral.couchcb

import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.ApiDocumentOperationResult
import nl.juraji.charactersheetscentral.couchcb.support.DocumentIdMeta
import org.springframework.core.ParameterizedTypeReference
import kotlin.reflect.KClass

abstract class CouchDbDocumentRepository<T : DocumentIdMeta>(
    private val couchDb: CouchDbService
) {
    abstract val databaseName: String
    abstract val documentClass: KClass<T>
    abstract val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<T>>

    fun findDocumentById(documentId: String): T? =
        couchDb.findDocumentById(databaseName, documentId, documentClass)

    fun findOneDocumentBySelector(query: DocumentSelector): T? =
        couchDb.findOneDocumentBySelector(databaseName, query.singleResult(), documentFindTypeRef)

    fun findDocumentsBySelector(query: DocumentSelector): List<T> =
        couchDb.findDocumentBySelector(databaseName, query, documentFindTypeRef)

    fun saveDocument(document: T): ApiDocumentOperationResult =
        couchDb.saveDocument(databaseName, document)

    fun deleteDocument(document: T) =
        couchDb.deleteDocument(databaseName, document)

    fun deleteDocument(documentId: String, documentRev: String) =
        couchDb.deleteDocument(databaseName, documentId, documentRev)
}
