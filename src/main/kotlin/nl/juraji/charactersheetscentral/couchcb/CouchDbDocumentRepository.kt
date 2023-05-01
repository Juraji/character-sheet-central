package nl.juraji.charactersheetscentral.couchcb

import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.ApiDocumentOperationResult
import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import nl.juraji.charactersheetscentral.couchcb.support.CreateIndexOperation
import nl.juraji.charactersheetscentral.couchcb.support.SaveAction
import org.springframework.core.ParameterizedTypeReference
import kotlin.reflect.KClass

abstract class CouchDbDocumentRepository<T : CentralDocument>(
    private val couchDb: CouchDbService
) {
    abstract val databaseName: String
    abstract val documentClass: KClass<T>
    abstract val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<T>>

    fun findDocumentById(documentId: String): T? =
        couchDb.findDocumentById(databaseName, documentId, documentClass)

    fun findOneDocumentBySelector(query: DocumentSelector<T>): T? =
        couchDb.findOneDocumentBySelector(databaseName, query.singleResult(), documentFindTypeRef)

    fun documentExistsBySelector(query: DocumentSelector<T>): Boolean =
        couchDb.documentExistsBySelector(databaseName, query)

    fun findDocumentsBySelector(query: DocumentSelector<T>): List<T> =
        couchDb.findDocumentBySelector(databaseName, query, documentFindTypeRef)

    fun saveDocument(document: T, action: SaveAction = SaveAction.AUTO): ApiDocumentOperationResult =
        couchDb.saveDocument(databaseName, document, action)

    fun deleteDocument(document: T) =
        couchDb.deleteDocument(databaseName, document)

    fun deleteDocument(documentId: String, documentRev: String) =
        couchDb.deleteDocument(databaseName, documentId, documentRev)

    open fun defineIndexes(): List<CreateIndexOperation> = emptyList()
}
