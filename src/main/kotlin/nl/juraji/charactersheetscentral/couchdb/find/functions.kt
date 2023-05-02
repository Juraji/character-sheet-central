@file:Suppress("unused")

package nl.juraji.charactersheetscentral.couchdb.find

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument

/** Selector typing (Clean code) */
typealias Selector = Map<String, Any>
typealias SelectorPair = Pair<String, Any>

/** Query creation */
/**
 * Convenience function to init a new [FindQuery].
 */
inline fun <reified T : CentralDocument> query(vararg selector: SelectorPair): FindQuery<T> =
    FindQuery(mapOf("modelType" to T::class.simpleName!!, *selector))

/** Query mutation */
/**
 * Map selector to match at most a single document.
 */
fun <T : CentralDocument> FindQuery<T>.singleResult(): FindQuery<T> =
    this.copy(limit = 1, skip = 0)

/**
 * Include only specific fields in the result documents.
 * Note: If [includeSelected] is true (default), the root fields in the selector are also included in the fields
 * to make it so CouchDB can use the most appropriate index.
 */
fun <T : CentralDocument> FindQuery<T>.withFields(
    vararg fields: String,
    includeSelected: Boolean = true
): FindQuery<T> {
    val selectFields = if (includeSelected) fields.toSet() + selector.keys else fields.toSet()
    return this.copy(fields = selectFields)
}

/**
 * Use a specific index for this query.
 * The [index] can be the name of a design document or index name
 */
fun <T : CentralDocument> FindQuery<T>.usingIndex(vararg index: String): FindQuery<T> =
    this.copy(useIndex = index.toSet())

/**
 * Append/merge field selectors to this instanceMa
 */
fun <T : CentralDocument> FindQuery<T>.appendSelectors(vararg selector: SelectorPair): FindQuery<T> =
    this.copy(selector = this.selector + selector.toMap())

/** Builders */
fun eq(key: String, value: Any): SelectorPair = key to value
fun eq(key: String, selector: Selector): SelectorPair = key to selector

/** Combination operators */
fun and(vararg selector: Selector): Selector = mapOf(Operators.AND to selector.toList())
fun or(vararg selector: Selector): Selector = mapOf(Operators.OR to selector.toList())
fun nor(vararg selector: Selector): Selector = mapOf(Operators.NOR to selector.toList())
fun all(vararg selector: Selector): Selector = mapOf(Operators.ALL to selector.toList())
fun not(selector: Selector): Selector = mapOf(Operators.NOT to selector)

/** Match operators */
fun lt(value: Any): Selector = mapOf(Operators.LT to value)
fun gt(value: Any): Selector = mapOf(Operators.GT to value)
fun lte(value: Any): Selector = mapOf(Operators.LTE to value)
fun gte(value: Any): Selector = mapOf(Operators.GTE to value)
fun eq(value: Any): Selector = mapOf(Operators.EQ to value)
fun ne(value: Any): Selector = mapOf(Operators.NE to value)
fun exists(exists: Boolean = true): Selector = mapOf(Operators.EXISTS to exists)
fun type(fieldType: FieldType): Selector = mapOf(Operators.TYPE to fieldType.typeName)
fun `in`(vararg values: Any): Selector = mapOf(Operators.IN to values)
fun nin(vararg values: Any): Selector = mapOf(Operators.NIN to values)
fun size(size: Int): Selector = mapOf(Operators.SIZE to size)
fun mod(divisor: Int, remainder: Int): Selector = mapOf(Operators.MOD to listOf(divisor, remainder))
fun regex(pattern: String): Selector = mapOf(Operators.REGEX to pattern)
fun elemMatch(selector: Selector): Selector = mapOf(Operators.ELEM_MATCH to selector)
fun allMatch(selector: Selector): Selector = mapOf(Operators.ALL_MATCH to selector)
fun keyMapMatch(selector: Selector): Selector = mapOf(Operators.KEY_MAP_MATCH to selector)
