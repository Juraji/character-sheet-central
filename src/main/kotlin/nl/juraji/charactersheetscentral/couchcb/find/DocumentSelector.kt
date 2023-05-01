package nl.juraji.charactersheetscentral.couchcb.find

import com.fasterxml.jackson.annotation.JsonProperty
import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument
import kotlin.reflect.KClass

/**
 * See https://docs.couchdb.org/en/stable/api/database/find.html for more information
 */
data class DocumentSelector<T : CentralDocument>(
    val selector: Map<String, Any>,
    val sort: List<Map<String, Any>>? = null,
    val limit: Int = 25,
    val skip: Int = 0,
    val fields: Set<String>? = null,
    @JsonProperty("use_index")
    val useIndex: Set<String>? = null
) {
    /**
     * Map selector to match only a single document
     */
    fun singleResult(): DocumentSelector<T> = this.copy(limit = 1, skip = 0)

    /**
     * Include only specific fields in the result documents.
     * Note: If [includeSelected] is true (default), the root fields in the selector are also included in the fields
     * to make it so CouchDB can use the most appropriate index
     */
    fun withFields(vararg fields: String, includeSelected: Boolean = true): DocumentSelector<T> {
        val selectFields = if (includeSelected) fields.toSet() + selector.keys else fields.toSet()
        return this.copy(fields = selectFields)
    }

    /**
     * Use a specific index for this query.
     * The [index] can be the name of a design document or index name
     */
    fun withIndex(vararg index: String): DocumentSelector<T> {
        return this.copy(useIndex = index.toSet())
    }

    /**
     * Append/merge field selectors to this instance
     */
    fun appendSelectors(vararg selector: Pair<String, Any>): DocumentSelector<T> =
        this.copy(selector = this.selector + selector.toMap())

    companion object {
        inline fun <reified T : CentralDocument> select(vararg selector: Pair<String, Any>): DocumentSelector<T> =
            DocumentSelector(mapOf("modelType" to T::class.simpleName!!, *selector))

        fun <T : CentralDocument> partialFilterSelector(
            modelType: KClass<T>,
            vararg selector: Pair<String, Any>
        ): DocumentSelector<T> =
            DocumentSelector(mapOf("modelType" to modelType.simpleName!!, *selector))
    }

    // Combination operators
    @Suppress("unused")
    object Combine {
        /** Matches if all the selectors in the array match. */
        const val AND = "\$and"

        /** Matches if any of the selectors in the array match. All selectors must use the same index. */
        const val OR = "\$or"

        /** Matches if the given selector does not match. */
        const val NOT = "\$not"

        /** Matches if none of the selectors in the array match. */
        const val NOR = "\$nor"
    }

    // Match operators
    @Suppress("unused")
    object Match {
        /** Match fields "less than" this one. */
        const val LT = "\$lt"

        /** Match fields "greater than" this one. */
        const val GT = "\$gt"

        /** Match fields "less than or equal to" this one. */
        const val LTE = "\$lte"

        /** Match fields "greater than or equal to" this one. */
        const val GTE = "\$gte"

        /** Match fields equal to this one. */
        const val EQ = "\$eq"

        /** Match fields not equal to this one. */
        const val NE = "\$ne"

        /** True if the field should exist, false otherwise. */
        const val EXISTS = "\$exists"

        /** One of: "null", "boolean", "number", "string", "array", or "object". */
        const val TYPE = "\$type"

        /** The document field must exist in the list provided. */
        const val IN = "\$in"

        /** The document field must not exist in the list provided. */
        const val NIN = "\$nin"

        /** Special condition to match the length of an array field in a document. Non-array fields cannot match this condition. */
        const val SIZE = "\$size"

        /**
         * Divisor and Remainder are both positive or negative integers.
         * Non-integer values result in a 404 status.
         * Matches documents where (field % Divisor == Remainder) is true, and only when the document field is an integer.
         * [divisor, remainder]
         */
        const val MOD = "\$mod"

        /** A regular expression pattern to match against the document field. Only matches when the field is a string value and matches the supplied regular expression. */
        const val REGEX = "\$regex"

        /** Matches an array value if it contains all the elements of the argument array. */
        const val ALL = "\$all"

        const val ELEM_MATCH = "\$elemMatch"
    }
}
