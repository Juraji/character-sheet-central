package nl.juraji.charactersheetscentral.couchdb

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import nl.juraji.charactersheetscentral.couchdb.documents.DocumentOpResult
import nl.juraji.charactersheetscentral.couchdb.documents.SaveType
import nl.juraji.charactersheetscentral.couchdb.find.*
import nl.juraji.charactersheetscentral.couchdb.indexes.CreateIndexOp
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
abstract class DocumentRepository<T : CentralDocument>(
    private val couchDb: CouchDbService
) {
    abstract val databaseName: String
    abstract val documentClass: KClass<T>
    abstract val documentFindTypeRef: ParameterizedTypeReference<FindResult<T>>
    val modelTypeSelector get() = eq("modelType", documentClass.simpleName!!)

    fun findDocumentById(documentId: String): T? =
        couchDb.findDocumentById(databaseName, documentId, documentClass)

    fun findOneDocumentBySelector(query: FindQuery<T>): T? =
        couchDb.findOneDocumentBySelector(
            databaseName,
            query.appendSelectors(modelTypeSelector).singleResult(),
            documentFindTypeRef
        )

    fun documentExistsBySelector(query: FindQuery<T>): Boolean =
        couchDb.documentExistsBySelector(
            databaseName,
            query.appendSelectors(modelTypeSelector)
        )

    fun findDocumentsBySelector(query: FindQuery<T>): List<T> =
        couchDb.findDocumentBySelector(
            databaseName,
            query.appendSelectors(modelTypeSelector),
            documentFindTypeRef
        )

    fun saveDocument(document: T, action: SaveType = SaveType.AUTO): DocumentOpResult =
        couchDb.saveDocument(databaseName, document, action)

    fun deleteDocument(document: T) =
        couchDb.deleteDocument(databaseName, document)

    fun deleteDocument(documentId: String, documentRev: String) =
        couchDb.deleteDocument(databaseName, documentId, documentRev)

    fun defineIndexes(): List<CreateIndexOp> = emptyList()
}
