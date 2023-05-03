package nl.juraji.charactersheetscentral.couchdb

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import nl.juraji.charactersheetscentral.couchdb.documents.DocumentOpResult
import nl.juraji.charactersheetscentral.couchdb.documents.SaveType
import nl.juraji.charactersheetscentral.couchdb.find.FindQuery
import nl.juraji.charactersheetscentral.couchdb.find.FindResult
import nl.juraji.charactersheetscentral.couchdb.find.singleResult
import nl.juraji.charactersheetscentral.couchdb.indexes.CreateIndexOp
import org.springframework.core.ParameterizedTypeReference
import kotlin.reflect.KClass

abstract class DocumentRepository<T : CentralDocument>(
    private val couchDb: CouchDbService
) {
    abstract val databaseName: String
    abstract val documentClass: KClass<T>
    abstract val documentFindTypeRef: ParameterizedTypeReference<FindResult<T>>

    protected fun findDocumentById(documentId: String): T? =
        couchDb.findDocumentById(databaseName, documentId, documentClass)

    protected fun findOneDocumentBySelector(query: FindQuery<T>): T? =
        couchDb.findOneDocumentBySelector(databaseName, query.singleResult(), documentFindTypeRef)

    protected fun documentExistsBySelector(query: FindQuery<T>): Boolean =
        couchDb.documentExistsBySelector(databaseName, query)

    protected fun findDocumentsBySelector(query: FindQuery<T>): List<T> =
        couchDb.findDocumentBySelector(databaseName, query, documentFindTypeRef)

    protected fun saveDocument(document: T, action: SaveType = SaveType.AUTO): DocumentOpResult =
        couchDb.saveDocument(databaseName, document, action)

    protected fun deleteDocument(document: T) =
        couchDb.deleteDocument(databaseName, document)

    protected fun deleteDocument(documentId: String, documentRev: String) =
        couchDb.deleteDocument(databaseName, documentId, documentRev)

    protected open fun defineIndexes(): List<CreateIndexOp> = emptyList()
}
