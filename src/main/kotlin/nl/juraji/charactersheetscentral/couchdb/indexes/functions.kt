package nl.juraji.charactersheetscentral.couchdb.indexes

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import nl.juraji.charactersheetscentral.couchdb.find.FindQuery
import kotlin.reflect.KClass

fun <T : CentralDocument> partialFilterSelector(
    modelType: KClass<T>,
    vararg selector: Pair<String, Any>
): FindQuery<T> = FindQuery(mapOf("modelType" to modelType.simpleName!!, *selector))
